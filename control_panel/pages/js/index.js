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
}

$(function() {

  rhizome.start(function(err) {
    if (err) {
      $('body').html('client failed starting : ' + err)
      throw err
    }

    rhizome.send('/sys/subscribe', ['/'])
  })

  rhizome.on('message', function(address, args) { 
    console.log('msg', address, args);
  })

  rhizome.on('connected', function() {
    alert('connected!')
  })

  rhizome.on('connection lost', function() {
    alert('connection lost!')
  })

  rhizome.on('server full', function() {
    alert('server is full!')
  })

});
