$(function(){
  var href = window.location.href ;
  var rawProtocol = window.location.protocol;
  var protocol = rawProtocol.substr( 0 , (rawProtocol.length-1) ) ;
  var hostname = window.location.hostname ;
  Object.keys(window.location).forEach(function(x,i,a){

  });;

  console.log('hostname : ' + JSON.stringify(hostname));
  var userId = 'testUserId';

  var webConn = null;
  function init(){
    webConn = new WebSocket('ws://' + hostname + ':18080/mahjong/ws?userId=' + userId);
    webConn.onopen = function(event) {
      console.log('onopen: ' + event.data);
    };

    webConn.onclose = function(event) {
      console.log('onclose');
    };

    webConn.onmessage = function(event) {
      if (event && event.data){
        console.log('event: ' + JSON.stringify(event));
        console.log('onmessage: ' + event.data);
      } else {
        console.log('event.prototype : ' + JSON.stringify(event.prototype));
        console.log('arguments.length : ' + JSON.stringify(arguments.length));
        console.log('event: ' + JSON.stringify(event));
        console.log('happen:');
      }
    };

    webConn.onerror = function(event) {
      console.log('onerror');
      init();
    };
  }
  init();

  function buildChatMessage(msg){
    var rtn = {
      "$type": "com.elseorand.game.mahjong.logic.GameMessage.ChatMessage",
      "senderId": userId,
      "message": msg
    };

    return rtn;
  }

  function buildRequestTsumohai(num){
    var rtn = {
      "$type": "com.elseorand.game.mahjong.logic.GameMessage.RequestTsumohai",
      "senderId": userId,
      "number": num
    };

    return rtn;
  }

  var el = {};
  var els_element = $('.element').each(function(){
    var _this = $(this);
    el[_this.attr('id')] = _this;
  });

  el.test.on('click', function(){
    var _this = $(this);
    var sendData = JSON.stringify(buildRequestTsumohai(1));
    webConn.send(sendData);
  });


});
