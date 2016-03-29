var sliders = [
  { name: 'depth' },
  { name: 'speed' },
  { name: 'amplitude' },
  { name: 'modulation' }
];
var items = sliders.length;
var band = 0;
var touchState = 0;

function drawBand(i) {
  var w = sliders[i].val * width;
  var h = height / items;
  fill(0);
  rect(0, i * h, width, h);

  fill(80);
  rect(0, i * h + h * 0.05, w, h * 0.9);
    
  sliders[i].div.position(10, i * h + h * 0.5);
}
function drawAll() {
  background(0);
  noStroke();
  for(var i=0; i<items; i++) {
    sliders[i].val = random(1);
    drawBand(i);
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
      sliders[band].val = touchX / width;
      drawBand(band);
      touchState = 0;
      break;
    case 2:
      sliders[band].val = touchX / windowWidth;
      drawBand(band);
      touchState = 0;
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
