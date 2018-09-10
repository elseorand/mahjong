$(function(){
  var href = window.location.href ;
  var rawProtocol = window.location.protocol;
  var protocol = rawProtocol.substr( 0 , (rawProtocol.length-1) ) ;
  var hostname = window.location.hostname ;
  Object.keys(window.location).forEach(function(x,i,a){
  });

  var el = {};
  el.login = $('#login')
  .on('click', function(){
    var _this = $(this);
    init(el.userId.val());
  });
  el.userId = $('#userId');

  console.log('hostname : ' + JSON.stringify(hostname));

  var webConn = null;
  function init(userId){
    while(webConn === null) {
      try {
        webConn = new WebSocket('ws://' + hostname + ':18080/mahjong/ws?userId=' + userId);
      } catch(e) {
        console.log('e.message : ' + e.message);
        webConn = null;
      }
    }
    webConn.onopen = function() {
      console.log('onopen: ' + hostname);
    };
    webConn.onclose = function(event) {
      console.log('onclose');
    };
    webConn.onmessage = function(event) {
      if (event && event.data){
        console.log('onmessage: ' + event.data);
        var rawObject = jsonParse(event.data);
        if (!rawObject.$type){
          console.log('rawObject : ' + JSON.stringify(rawObject));
          return ;
        }
        var obj = builders[rawObject.$type](rawObject);
        if (obj.run){
          obj.run();
        }
      } else {
        console.log('event.prototype : ' + JSON.stringify(event.prototype));
        console.log('arguments.length : ' + JSON.stringify(arguments.length));
        console.log('event: ' + JSON.stringify(event));
        console.log('happen:');
      }
    };

    webConn.onerror = function(event) {
      webConn = null;
      console.log('onerror');
      init();
    };
  }


  var builders = {};
  builders.ChatMessage = function(msg){
    var rtn = {
      "$type": "ChatMessage",
      "senderId": userId,
      "message": msg
    };
    rtn.run = function(){
      console.log(JSON.stringify(rtn));
    };
    return rtn;
  };

  builders.IhaveTsumohai = function(num){Ｏ
    var rtn = {
      "$type": "IhaveTsumohai",
      "senderId": userId,
      "number": num
    };
    rtn.run = function(){
      console.log(JSON.stringify(rtn));
    };
    return rtn;
  };

  builders.YouHaveTsumohai = function(rawData){
    console.log('rawData YouHaveTsumohai : ' + JSON.stringify(rawData));
    rawData.paiList.forEach(function(pai){
        pai.unicode = '&#' + pai.unicode + ';';
    });
    rawData.run = function(){
      rawData.paiList.forEach(function(pai){
        $('<div id="pai_id_' + pai.id + '" class="element paiObject" ' +
          ' data-pai-id="' + pai.id +
          '" data-pai-number="' + pai.number +
          '" data-pai-type="' + pai.paiType.name +
          '">' +
          '  <section id="" class="element paiBack" ></section>' +
          '  <section id="" class="element paiBody" ></section>' +
          '  <span class="pai ' + pai.paiType.name + '">' + pai.unicode + '</span>' +
          '  <section id="" class="element paiFront" ></section>' +
          '</div>').appendTo(el.holdingPaiListArea0);
      });

    };
    return rawData;
  };

  var el = {};
  var els_element = $('.element').each(function(){
    var _this = $(this);
    el[_this.attr('id')] = _this;
  });

  el.test.on('click', function(){
    var _this = $(this);
    var sendData = JSON.stringify(builders.IhaveTsumohai(1));
    try {
      if (!webConn){
        init();
      }
      webConn.send(sendData);
    } catch(e) {
      console.log(e);
      init();
      webConn.send(sendData);
    }

  });

  function jsonParse(raw) {
    try{
      return JSON.parse(raw);
    }catch(e){
      console.log('raw : ' + raw);
      return raw;
    }
  }
});
