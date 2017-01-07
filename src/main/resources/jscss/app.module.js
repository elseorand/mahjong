$(function(){
  var webConn = new WebSocket('ws://localhost:18080/mahjong/ws');
  webConn.onopen = function(event) {
    console.log('onopen');
  };

  webConn.onclose = function(event) {
    console.log('onclose');
  };

  webConn.onmessage = function(event) {
    if (event && event.datta){
      console.log('onmessage: ' + event.data);
    }
  };

  webConn.onerror = function(event) {
    console.log('onerror');
  };

});
