# Convert Digital Image to Perler Bead Template

## Synopsis
This program allows the user to create a custom Perler bead design by converting a digital image into a printable template. I created this for my final project in CSCI 1115 Algorithms and Data Structures at Southwest Technical College.

## Motivation
My children love playing with Perler beads. Often they look up designs online and copy them from the screen. I noticed that one of the pegboards we own is transparent and was likely designed for placing a template underneath it. I thought creating a program that could create custom templates would be a fun and useful project.

## How to Run
All methods and classes are contained in a single java file: ConvertImageToTemplate. In addition to this file, a PNG file that is exactly 29x29px is required - I've included several PNG files in the repository for use. The name and path for the PNG file must be entered manually on line 26 of the java file. Once this line has been modified and saved, run the program, and a file named BeadTemplate.png should be exported.

## Code Example
This program works by reading each pixel of an image one at a time and comparing the color values of that pixel with a predetermined color palette, namely, the standard bead colors produced by Perler. It determines the closest match and stores that information in a multidimensional array. A new PNG file is exported with a graphic template of the design and list of what bead colors are needed for the project. When printed properly on standard letter paper, it should be the perfect size for the template.

![Illustration of what program does](illustration)

The most difficult part of this process was figuring out how to compare the color value of each pixel to a predetermined palette of colors. The process is somewhat convoluted, and I’m convinced that a more efficient process exists given more time and some creativity. That said, the process does work and can be easily modified to use a larger or smaller color pallete. By default, all 52 standard colors of Perler beads are included in the color palette.

The following method is used to determine if a suitable color match has been found for the pixel in question. Using standard RGB values, the red channel of the pixel is examined first, and the palette color with the nearest red channel value is identifed and flagged as a possible match using this method. The green and blue channels are then examined. When a palette color has been flagged on all three channels, it is confirmed as the best match and saved as the correct bead color for that position of the mosaic. Otherwise, the program continues to loop through the color channels expanding its search to the next nearest value for that channel.

```
public boolean addBeadAsPossibleMatch (Map map, String key, int channel) {
	if(map.containsKey(key)) {
		addToChannel((ColorMatch)map.get(key), channel);
	} else {
		map.put(key, new ColorMatch(key));
		addToChannel((ColorMatch)map.get(key), channel);
	}
	if(isMatch((ColorMatch)map.get(key))) {
		return true;
	} else {
		return false;
	}
}
```
