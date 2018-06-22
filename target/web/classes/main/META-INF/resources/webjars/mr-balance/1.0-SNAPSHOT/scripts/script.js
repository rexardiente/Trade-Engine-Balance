import $ from 'jquery';
import _ from 'lodash';
import DateTime from './dateTime.js';

/* Variable and constant declarations */
var LOGIN_SUCCESS_STATUSES = [201, 204, 409];
var depth = 4;

var $storage = window.localStorage;
var $loginPage = $('#login-page');
var $innerPage = $('#inner-page');

var $loginForm = $loginPage.find('form');

var $accountId = $loginPage.find('input');
var $fieldset = $loginPage.find('fieldset');

var $menuItems = $('li.menu a');

var wsMrBalance;
var wsMrTradeEngine;
var usersOpenOrders;
var currentPair;
/* End of Variable and constant declarations */

/* Initialization */
handleSession(getAccountID());

$loginForm.on('submit', function(event) {
  event.preventDefault();
  requestLogin($loginForm.serialize());
});

$menuItems.on('click', function(ev) {
  ev.preventDefault();
  $('main section').hide();
  $(ev.target.hash).show();
  $menuItems.removeClass('active');
  $(this).addClass('active');
});

$('#refresh-balance').on('click', function(ev) {
  ev.preventDefault();
  getBalance();
});

$('#refresh-transaction-history').on('click', function(ev) {
  ev.preventDefault();
  getUserTransactionHistory();
  getUserFundsHistory();
});

$('#buy-order-form, #sell-order-form').on('submit', handleTransactionEvent('order', function(data) {
  return _(data)
    .omit(['total', 'market', 'price'])
    .set('price', data.market? '-1': data.price)
    .set('id_currency_base', currentPair[0])
    .set('id_currency_counter', currentPair[1])
    .value();
}));

$('.base').on('change', syncSelect('.counter'));
$('.counter').on('change', syncSelect('.base'));

$('#deposit-form, #withdraw-form').on('submit', handleTransactionEvent('funds', function(data) {
  return _(data)
    .omit(['is_deposit'])
    .set('is_deposit', data.is_deposit === 'true'? true: false)
    .value();
}));

$('#open-orders-table').on('click', '.cancel-order', function(ev) {
  ev.preventDefault();
  var order = _.find(usersOpenOrders, {'id_order': $(ev.target).attr('data-id')});
  if(!_.isEmpty(order)) {
    wsSendMrBalance({
      'command': 'cancel_order',
      'id_account_ref': getAccountID(),
      'side': order.side.toUpperCase(),
      'amount': order.amount,
      'price': order.price,
      'id_currency_base': currentPair[0],
      'id_currency_counter': currentPair[1],
      'id_order': order.id_order
    });
  }
});

$('#currency-pair').on('change', function(ev) {
  ev.preventDefault();
  setPair();
  getOpenOrders();
  getOrderbook();
});

$('#buy-price, #sell-price, #buy-amount, #sell-amount').on('input', function(ev) {
  validateInput(ev);
  getTotal(ev);
});

$('#deposit-amount, #withdraw-amount').on('input', validateInput);

$('#logout-account').on('click', function(ev) {
  ev.preventDefault();
  handleSession(false);
  customAlert('Successfully logged out.', 'fail');
});

$('.market-order').on('click', function(ev) {
  var priceField = $(ev.target).parents('form').find('.price');
  if(priceField.is(':disabled')) {
    priceField.prop('disabled', false);
  } else {
    priceField.val('').trigger('input');
    priceField.prop('disabled', true);
  }
});
/* End of Initialization */

/* Function Declarations */
function getAccountID() {
  return $storage.getItem('accountId');
}

function validateInput(ev) {
  var value = $(ev.target).val();
  if(value > 0) {
    ev.target.setCustomValidity('');
    return true;
  } else {
    ev.target.setCustomValidity('Input must be greater than 0.');
    return false;
  }
}

function getTotal(ev) {
  var price = $(ev.target).parents('form').find('[name="price"]').val();
  var amount = $(ev.target).parents('form').find('[name="amount"]').val();
  $(ev.target).parents('form').find('[name="total"]').val(price * amount);
}

function setPair() {
  if(!_.isUndefined($('#currency-pair').val())) {
    var pair = $('#currency-pair').val().split('/');
    currentPair = pair;
    $('.base-currency').html(pair[0]);
    $('.counter-currency').html(pair[1]);
  }
}

function requestLogin(data) {

  $fieldset.attr('disabled', true);

  return $.ajax({
    type: 'POST',
    url: '/login',
    data: data
  })

  .always(function(data, textStatus, xhr) {
    if(!_.isUndefined(xhr.status)){
      handleSession(~LOGIN_SUCCESS_STATUSES.indexOf(xhr.status));
    } else {
      $fieldset.attr('disabled', false);
      customAlert('Invalid Account ID', 'fail');
    }
  });

}

function handleSession(isLoggedIn) {

  if(isLoggedIn) {
    $loginPage.hide();
    $innerPage.show();

    $storage.setItem('accountId', $accountId.val() || getAccountID());

    setPair();
    $('#current-account, .current-account').html(getAccountID());
    websocketHandshake();
    customAlert('Successfully logged in', 'success');
  } else {
    $loginPage.show();
    $innerPage.hide();
    $('#inner-page form').trigger('reset');

    $storage.removeItem('accountId');

    if(wsMrBalance) {
      wsMrBalance.close();
      wsMrTradeEngine.close();
    }
  }

  $fieldset.attr('disabled', !!isLoggedIn);
  $('form').trigger('reset');

}

function wsSendMrBalance(message) {
  if(wsMrBalance.readyState === 1) {
    wsMrBalance.send(JSON.stringify(message));
  } else {
    customAlert('Mr. Balance connection is not yet ready.', 'fail');
  }
}

function wsSendMrTradeEngine(message) {
  if(wsMrTradeEngine.readyState === 1) {
    wsMrTradeEngine.send(JSON.stringify(message));
  } else {
    customAlert('Mr. Trade Engine connection is not yet ready.', 'fail');
  }
}

function orderbook(data) {
  _(data.response)
    .pick(['bids', 'asks'])
    .each(showOrderbook);
}

function showOrderbook(orders, key) {
  var table = $('#' + key + '-table tbody'),
      sum = 0, totalCounter = 0;
  if(!_.isEmpty(orders)) {
    orders = _(orders);

    if(key === 'asks') {
      orders = orders.reverse();
    }

    table.html(orders.map(function(order) {
      totalCounter = order.size * order.price;
      sum += totalCounter;
      return '<tr><td>'+ order.price.toFixed(depth) +'</td>' +
             '<td>'+ order.size.toFixed(depth) +'</td>' +
             '<td>'+ totalCounter.toFixed(depth) +'</td>' +
             '<td>'+ sum.toFixed(depth) +'</td></tr>';
    }).join(''));
  } else {
    table.html('<tr><td colspan="'+ $('#'+ key +'-table th').length +
      '" class="center-text">No Results.</td></tr>');
  }
}

function showPairlist(data) {
  var table = $('#pairlist-table tbody');
  if(!_.isEmpty(data.response)) {
    table.html(_.map(data.response, function(pair) {
      return '<tr>'+
          '<td>'+ pair.currency_base + '/' + pair.currency_counter +'</td>'+
          '<td>'+ pair.latest_price.toFixed(depth) +'</td>'+
          '<td>'+ pair.change_24hr.toFixed(depth) +'</td>'+
          '<td>'+ pair.volume_24hr.toFixed(depth) +'</td>'+
        '</tr>';
    }).join(''));
  } else {
    table.html('<tr><td colspan="4" class="center-text">No Results.</td></tr>');
  }
}

function getBalance() {
  wsSendMrBalance({
    'command': 'get_balance',
    'id_account_ref': getAccountID()
  });
}

function getUserTransactionHistory() {
  wsSendMrBalance({
    'command': 'transaction_history',
    'id_account_ref': getAccountID()
  });
}

function getUserFundsHistory() {
  wsSendMrBalance({
    'command': 'funds_history',
    'id_account_ref': getAccountID()
  });
}

function getOpenOrders() {
  wsSendMrBalance({
    'command' : 'open_orders',
    'id_account_ref' : getAccountID(),
    'currency_base': currentPair[0],
    'currency_counter': currentPair[1]
  });
}

function getPairlist() {
  wsSendMrTradeEngine({
    'command': 'pairlist',
    'id_account_ref' : getAccountID()
  });
}

function getOrderbook() {
  wsSendMrTradeEngine({
    'command': 'order_book',
    'id_account_ref' : getAccountID(),
    'currency_base': currentPair[0],
    'currency_counter': currentPair[1],
    'levels': ''
  });
}

//testable
function parseSerializedArray(array) {
  return _(array).map(_.values).fromPairs().value();
}

function showBalance(data) {
  var table = $('#balance-table tbody');
  data = _.orderBy(data.response, ['id_currency'], ['asc']);
  if(!_.isEmpty(data)) {
    table.html(_.map(data, function(currency) {
      if(currency.balance > 0) {
        return '<tr>' +
          '<td>'+ currency.id_currency +'</td>' +
          '<td>'+ currency.balance.toFixed(depth) +'</td>' +
          '<td>'+ currency.balance.toFixed(depth) +'</td>' +
          '<td>'+ '0.00' +'</td>' +
          '</tr>';
      }
    }).join(''));
  } else {
    table.html('<tr><td colspan="4" class="center-text">No Results.</td></tr>');
  }
}

function showTransactionHistory(data) {

  var table = $('#trade-history'),
      dateTime;

  if(!_.isEmpty(data)) {
    table.html(data.map(function(tx) {
      dateTime = DateTime.get(tx.created_at);
      return '<tr>' +
          '<td>'+ dateTime[0] +'</td>'+
          '<td>'+ dateTime[1] +'</td>' +
          '<td>'+ tx.side +'</td>' +
          '<td>'+ tx.amount +'</td>' +
          '<td>'+ tx.id_currency_base + '/' + tx.id_currency_counter +'</td>' +
          '<td>'+ tx.price +'</td>' +
          '<td>'+ tx.trade_type +'</td></tr>';
    }).join(''));
  } else {
    table.html('<tr><td colspan="7" class="center-text">No Transaction History</td></tr>');
  }

}

function showFundsHistory(data) {

  var table = $('#funds-history'),
      dateTime;

  if(!_.isEmpty(data)) {
    table.html(data.map(function(tx) {
      dateTime = DateTime.get(tx.created_at);
      return '<tr>' +
          '<td>'+ dateTime[0] +'</td>'+
          '<td>'+ dateTime[1] +'</td>' +
          '<td>'+ tx.payment_type +'</td>' +
          '<td>'+ tx.amount +'</td>' +
          '<td>'+ tx.id_currency +'</td>';
    }).join(''));
  } else {
    table.html('<tr><td colspan="5" class="center-text">No Funds History</td></tr>');
  }

}

function showOpenOrders(data) {
  var table = $('#open-orders-table tbody'),
      dateTime;

  usersOpenOrders = data.response;

  if(!_.isEmpty(data.response)) {
    table.html(_.map(data.response, function(order) {
      dateTime = DateTime.get(order.created_at);
      return '<tr>' +
          '<td>'+ dateTime[0] +'</td>'+
          '<td>'+ dateTime[1] +'</td>' +
          '<td>'+ order.side +'</td>' +
          '<td>'+ order.amount +'</td>' +
          '<td>'+ order.price +'</td>' +
          '<td>'+
            '<a class="cancel-order" href="javascript:;" data-id="'+ order.id_order +'">'+
              'Cancel'+
            '</a>'+
          '</td>' +
        '</tr>';
    }).join(''));
  } else {
    table.html('<tr><td colspan="7" class="center-text">No Open Orders</td></tr>');
  }
}

function close(ev) {
  console.log('Disconnected: ', ev);
}

function open(ev) {
  console.log('Connected: ', ev);
}

function message(ev) {
  var data = JSON.parse(ev.data);
  switch(data.message) {
    case 'broadcast':
      orderbook(data);
      break;
    case 'order_book':
      orderbook(data);
      break;
    case 'pairlist':
      showPairlist(data);
      break;
    case 'deposit':
      customAlert(data.response.description, data.response.status);
      getBalance();
      $('#deposit-form fieldset').attr('disabled', false);
      break;
    case 'withdraw':
      customAlert(data.response.description, data.response.status);
      getBalance();
      $('#withdraw-form fieldset').attr('disabled', false);
      break;
    case 'get_balance':
      showBalance(data);
      break;
    case 'order':
      customAlert(data.response.description, data.response.status);
      getBalance();
      getOpenOrders();
      getOrderbook();
      $('#place-order-container fieldset').attr('disabled', false);
      break;
    case 'transaction_history':
      showTransactionHistory(data.response);
      break;
    case 'funds_history':
      showFundsHistory(data.response);
      break;
    case 'open_orders':
      showOpenOrders(data);
      break;
    case 'cancel_order':
      customAlert(data.response.description, data.response.status);
      getBalance();
      getOpenOrders();
      break;
    default:
      console.log('Server Message: ', ev);
  }
}

function error(ev) {
  console.log('Error: ', ev);
}

function syncSelect(selector) {
  return function(ev) {
    $(ev.target)
      .siblings(selector)
      .find('option[value="' + ev.target.value + '"]')
      .attr('disabled', true)
      .removeAttr('selected')
      .siblings()
      .removeAttr('disabled');
  };
}

function handleTransactionEvent(command, transform) {

  transform = transform || _.identity;

  return function(ev) {

    ev.preventDefault();

    var form = $(this),
      data = parseSerializedArray(form.serializeArray()),
      result = _.assign({
        command: command,
        id_account_ref: getAccountID()
      }, data);

    form.find('fieldset').attr('disabled', true);

    wsSendMrBalance(transform(result));

  };

}

function websocketHandshake() {
  wsMrBalance = new WebSocket('ws://'+mr.balanceURL+'/');
  wsMrTradeEngine = new WebSocket('ws://'+mr.tradeEngineURL+'/');

  wsMrTradeEngine.onopen = function(ev) {
    open(ev);
    getOrderbook();
    keepMrTradeEngineAlive();
  };
  wsMrTradeEngine.onmessage = function(ev) { message(ev); };
  wsMrTradeEngine.onclose = function(ev) { close(ev); };
  wsMrTradeEngine.onerror = function(ev) { error(ev); };

  wsMrBalance.onopen = function(ev) {
    open(ev);
    getUserTransactionHistory();
    getUserFundsHistory();
    getOpenOrders();
    keepMrBalanceAlive();
  };
  wsMrBalance.onmessage = function(ev) { message(ev); };
  wsMrBalance.onclose = function(ev) { close(ev); };
  wsMrBalance.onerror = function(ev) { error(ev); };

}

function customAlert(msg, type) {
  $('#error-message p').hide();
  $('#error-message')
  .find('#' + type)
  .text(_.capitalize(msg))
  .fadeIn(function() {
    $(this).fadeOut(1000);
  });
}

function keepMrBalanceAlive() {
  getBalance();
  setTimeout(keepMrBalanceAlive, 60000);
}

function keepMrTradeEngineAlive() {
  getPairlist();
  setTimeout(keepMrTradeEngineAlive, 60000);
}
/* End of Function Declarations */
