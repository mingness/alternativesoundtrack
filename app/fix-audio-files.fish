#!/usr/bin/fish

for file in audio/*.wav
  set lines (soxi $file)

  # Not stereo
  if echo $lines[3] | grep -q 2
  else 
    echo $lines[2] $lines[3];
    sox $file -c 2 tmp.wav
    mv tmp.wav $file
  end

  # Not right sample rate
  if echo $lines[4] | grep -q 44100
  else 
    echo $lines[2] $lines[4];
    sox $file -r 44100 tmp.wav
    mv tmp.wav $file
  end
end




