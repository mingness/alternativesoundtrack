// see nexusosc.con

nx.colors.black = "#CCCCCC";
nx.onload = function() {
  nx.sendsTo('js');

  for (var key in nx.widgets) {
    var w = nx.widgets[key];
    // set scrollbars to relative
    if(w.mode == 'absolute') {
      w.mode = 'relative';
    }
    // set buttons to impulse
    if(w.mode == 'aftertouch') {
      w.mode = 'impulse';
    }
    with(nx.widgets[key]) {
      on('*', function(data) {
        rhizome.send(canvasID, [data.press || data.value]);
      });
    }
  }
}

var maskCanvas, maskContext, screenshot;
var prPingTime, scPingTime = new Date().getTime();

function send_pr_state() {
  if ((new Date().getTime() - prPingTime) < 3000) { 
    $('#pr_on').text("Running");
  } else {
    $('#pr_on').text("OFF");
  }
}
function send_sc_state() {
  if ((new Date().getTime() - scPingTime) < 1000) { 
    $('#sc_on').text("Running");
  } else {
    $('#sc_on').text("OFF");
  }
}  

$(function() {
  maskCanvas = document.getElementById('mask');
  maskContext = maskCanvas.getContext('2d');

  rhizome.start(function(err) {
    if (err) {
      $('body').html('client failed starting : ' + err)
      throw err
    }

    rhizome.send('/sys/subscribe', ['/panel'])
  });

  rhizome.on('message', function(address, args) {
    if(address === '/panel/p5') {
      prPingTime = new Date().getTime();
    }
    if(address === '/panel/video_time') {
      var w = nx.widgets["/p5/video_time"];
      w.val.value = args[0];
      w.draw();
    }
    if(address === '/panel/screenshot') {
      window.setTimeout(function() {
        screenshot = new Image();
        screenshot.onload = function() {
          maskContext.drawImage(screenshot, 0, 0, 640, 480);
        };
        screenshot.src = 'screenshot/screenshot.jpg?' + Math.random();
      }, 200);
    }
    if(address === '/panel/a_of') {
      nx.widgets['/p5/a_of'].set({value: args[0]});
    }
    if(address === '/panel/bgsub') {
      nx.widgets['/p5/bgsub'].set({value: args[0]});
    }
    if(address === '/panel/of_regression') {
      nx.widgets['/p5/of_regression'].set({value: args[0]});
    }
    if(address === '/panel/of_smoothness') {
      nx.widgets['/p5/of_smoothness'].set({value: args[0]});
    }
    if(address === '/panel/mask_enabled') {
      nx.widgets['/p5/mask_enabled'].set({value: args[0]});
    }
    if(address === '/panel/display_enabled') {
      nx.widgets['/p5/display_enabled'].set({value: args[0]});
    }
    if(address === '/panel/sc_files') {
      $('#soundFile1').text(args[0]);
      $('#soundFile2').text(args[1]);
    }
    if(address === '/panel/sc_params') {
      scPingTime = new Date().getTime();
      nx.widgets['/sc/testA'].set({value: args[0]});
      nx.widgets['/sc/testB'].set({value: args[1]});
      nx.widgets['/sc/testC'].set({value: args[2]});
    }
  });

  rhizome.on('connected', function() {
    alert('connected!')
  });

  rhizome.on('connection lost', function() {
    alert('connection lost!')
  });

  rhizome.on('server full', function() {
    alert('server is full!')
  });

  maskCanvas.width = 640;
  maskCanvas.height = 480;

  screenshot = new Image();
  screenshot.onload = function() {
    maskContext.drawImage(screenshot, 0, 0, 640, 480);
  };
  screenshot.src = 'screenshot/screenshot.jpg';

  var maskClick = function(e) {
    var x, y;
    if (e.pageX || e.pageY) {
      x = e.pageX;
      y = e.pageY;
    } else {
      x = e.clientX + document.body.scrollLeft + document.documentElement.scrollLeft;
      y = e.clientY + document.body.scrollTop + document.documentElement.scrollTop;
    }
    x -= maskCanvas.offsetLeft;
    y -= maskCanvas.offsetTop;

    maskContext.beginPath();
    maskContext.fillStyle = "#FF00FF";
    maskContext.arc(x, y, 50, 0, Math.PI*2);
    maskContext.fill();

    rhizome.send('/p5/add_mask_point', [x / 640, y / 480]);
  };
  $('#mask').on('click', maskClick);
  $('#mask').on('touch', maskClick);

  setInterval(send_pr_state, 1000);
  setInterval(send_sc_state, 1000);
});
