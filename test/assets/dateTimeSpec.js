var assert = require('chai').assert;
var projPath = '/Volumes/Development/Projects/mr-balance/app/assets/scripts/';

describe('getDatetime', function() {
  it('Return an array', function() {
    var dateTime = require(projPath + 'dateTime.js');
    var result = dateTime.get('2018-04-16T03:40:08.612262Z');
    assert.equal(Array.isArray(result), true);
  });
});
