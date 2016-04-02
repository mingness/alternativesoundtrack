var sliders = [
  { name: 'A' },
  { name: 'B' },
  { name: 'C' },
  { name: 'D' }
];
var items = sliders.length,
  band = 0, 
  value = 0,
  touchState = 0;

function drawBand(band, val) {
  var w = val * width;
  var h = height / items;
  fill(0);
  rect(0, band * h, width, h);

  fill(80);
  rect(0, band * h + h * 0.05, w, h * 0.9);
    
  sliders[band].div.position(10, band * h + h * 0.5);
}
function drawAll() {
  background(0);
  noStroke();
  for(var i=0; i<items; i++) {
    sliders[i].val = random(1);
    drawBand(i, sliders[i].val);
  }
}
function setup() {
  createCanvas(windowWidth, windowHeight);
  for(var i=0; i<items; i++) {
    sliders[i].div = createDiv(sliders[i].name);
    sliders[i].div.style('color', '#CCC');
  }
  drawAll();
}
function draw() {
  switch(touchState) {
    case 1:
      band = Math.floor(items * touchY / windowHeight);
    case 2:
      value = touchX / windowWidth;
      sliders[band].val = value;
      touchState = 0;
      rhizome.send('/slider', [band, value])
      break;
  }
}
function touchStarted() {
  touchState = 1;
  return false;
}
function touchMoved() {
  touchState = 2;
  return false;
}
function windowResized() {
  resizeCanvas(windowWidth, windowHeight);
  drawAll();
}
    
$(function() {

  rhizome.start(function(err) {
    if (err) {
      $('body').html('client failed starting : ' + err)
      throw err
    }

    // subscribe to all messages
    rhizome.send('/sys/subscribe', ['/'])
  })

  rhizome.on('message', function(address, args) { 
    drawBand(args[0], args[1]);
    console.log(address, args);
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

})
