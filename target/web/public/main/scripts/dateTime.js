function get(date) {
  return new Date(date)
    .toLocaleString('en-Us', { hour12: true })
    .replace(' ', '')
    .split(',');
}

exports.get = get;
