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
  // show display enabled by default
  nx.widgets['/p5/display_enabled'].val.value = 1;
  nx.widgets['/p5/display_enabled'].draw();
  // set default value for sliders
  nx.widgets['/p5/of_regression'].set({value: 0.5});
  nx.widgets['/p5/of_smoothness'].set({value: 0.5});
  nx.widgets['/p5/video_time'].set({value: 0.5});
  nx.widgets['/sc/testA'].set({value: 0.5});
  nx.widgets['/sc/testC'].set({value: 0.1});
  nx.widgets['/sc/testB'].set({value: 0.0});

}

var maskCanvas, maskContext, screenshot;

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

});
