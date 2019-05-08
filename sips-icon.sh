#!/bin/bash
if [ -z "$1" ] ; then
echo "usage: sips-icon pngName"
exit 0

fi

sips -z 20 20     $1 --out icon-20.png
sips -z 60 60     $1 --out icon-20@3x.png
sips -z 40 40     $1 --out icon-40.png
sips -z 80 80     $1 --out icon-40@2x.png
sips -z 50 50     $1 --out icon-50.png
sips -z 100 100     $1 --out icon-50@2x.png
sips -z 120 120     $1 --out icon-60@2x.png
sips -z 180 180     $1 --out icon-60@3x.png
sips -z 72 72     $1 --out icon-72.png
sips -z 144 144     $1 --out icon-72@2x.png
sips -z 76 76     $1 --out icon-76.png
sips -z 152 152     $1 --out icon-76@2x.png
sips -z 167 167     $1 --out icon-83.5@2x.png
sips -z 29 29     $1 --out icon-small.png
sips -z 58 58     $1 --out icon-samll@2x.png
sips -z 87 87     $1 --out icon-small@3x.png
sips -z 57 57     $1 --out icon.png
sips -z 114 114     $1 --out icon@2x.png