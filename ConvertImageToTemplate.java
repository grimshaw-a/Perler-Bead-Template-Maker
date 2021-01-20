/**
 * <h1>Perler Bead Template Creator</h1>
 * <p>This program allows the user to take a digital image and convert it into a printable pattern 
 * for a Perler bead pegboard. The user must submit a 29x29px image. The program then finds the closest
 * matching bead color for each pixel from a specific color palette. A printable file is then exported 
 * which includes the template pattern and a list of each bead color needed and in what quantity.</p>
 * @author Adam Grimshaw
 * Date: 08/26/2020
 * Course: CSCI 1115 Algorithms and Data Structures, Southwest Technical College 
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;

public class ConvertImageToTemplate {
	/**
	 * The main method. Specifies file to be uploaded, and calls all methods needed to convert the image
	 * to a printable template. 
	 * @param args Not used.
	 */
	public static void main(String args[]) {
		// Name of file to be imported and converted to bead pattern.
		String fileName = "painting-03.png";
		
		// Create new color library and new empty array of beads.
		ColorLibrary beadPalette = new ColorLibrary();
		ArrayList<Bead> paletteArray = new ArrayList<>();
		
		// Populate bead array with all bead colors.
		beadPalette.populateArrayAllColors(paletteArray);
		
		// Create a map with all bead colors(key) and their color codes(value).
		Map<String,String> codeMap = beadPalette.createCodeMap(paletteArray);
		
		// Create and run a converter to sample each pixel value of the imported file and find the closest matching bead color in the palette. Returns a BeadMatrix which can be processed into a graphic.
		ImageToBeadConverter converter = new ImageToBeadConverter();
		BeadMatrix beadTemplate = converter.processImage(fileName, paletteArray);
		
		// Feed the BeadMatrix and codeMap into an exporter where the information will be turned into a graphic and exported as an image file.
		CreateImageAndExportToFile exporter = new CreateImageAndExportToFile(fileName, beadTemplate, codeMap);
	}
}

/**
 * This class creates a converter to import an image file and then convert each pixel into a bead object.
 * @author Adapted from code found at {@link https://www.tutorialspoint.com/java_dip/understand_image_pixels.htm}
 */
class ImageToBeadConverter {
	private BufferedImage image;
	private int width;
	private int height;
	
	/** Default constructor.*/
	public ImageToBeadConverter() {
	}
	
	/**
	 * Imports an image and reads through it one pixel at a time. Each pixel's color value is compared 
	 * against the color value of bead objects in the color palette until the closest match is found. 
	 * @param fileName The name of the image file to import and read.
	 * @param beadArray An ArrayList containing bead objects. Each bead object represents a different
	 * color in the available color palette.
	 * @return Returns a BeadMatrix object. 
	 */
	public BeadMatrix processImage(String fileName, ArrayList<Bead> beadArray) {
		// Create bead color palette. Creates an array for each color channel.
		ArrayList<Bead> redChannelArray = new ArrayList<>(beadArray);
		ArrayList<Bead> greenChannelArray = new ArrayList<>(beadArray);
		ArrayList<Bead> blueChannelArray = new ArrayList<>(beadArray);
		// Sorts each array by specific color channel.
		Collections.sort(redChannelArray, new RedChannelComparator());
		Collections.sort(greenChannelArray, new GreenChannelComparator());
		Collections.sort(blueChannelArray, new BlueChannelComparator());
		
		try {
			// The image file to be read.
			File input = new File(fileName);
			image = ImageIO.read(input);
			width = image.getWidth();
			height = image.getHeight();
			
			// An array to store bead objects. Each bead represents one pixel.
			Bead[][] beadGrid = new Bead[height][width];
			
			// A map to store a list of all bead colors used in the design, and how many of each color are needed.
			Map<String, Integer> colorList = new HashMap<>();
			int count = 0; // An index number for each pixel processed. Only used in print statements.
			
			// Loops through the entire image one pixel at a time starting at top left corner.
			for(int i=0; i<height; i++) {
				for(int j=0; j<width; j++) {
					count++;
					// Get RGB color value for current pixel.
					Color c = new Color(image.getRGB(j, i));
					//System.out.println("S.No: " + count + " Red: " + c.getRed() +"  Green: " + c.getGreen() + " Blue: " + c.getBlue());
					
					// Create starting index for search on each color channel. Find closest matching value in each channel as initial point of search.
					int k = 0;
					while(c.getRed() > redChannelArray.get(k).getColor().getRed()) {
						k++;
					}
					int redIndex = k;
					k = 0;
					while(c.getGreen() > greenChannelArray.get(k).getColor().getGreen()) {
						k++;
					}
					int greenIndex = k;
					k = 0;
					while(c.getBlue() > blueChannelArray.get(k).getColor().getBlue()) {
						k++;
					}
					int blueIndex = k;
					//System.out.println("RedIndex: " + redIndex + " GreenIndex: " + greenIndex + " BlueIndex: " + blueIndex);
					
					// Create map to hold all possible bead colors for this pixel. 
					Map<String,ColorMatch> colorMap = new HashMap<>();
					
					// Create ColorMatch object for use in calling ColorMatch methods.
					ColorMatch masterMatch = new ColorMatch();
					
					// Boolean to control the while loop.
					boolean stopSearching = false;
					
					// Index integer to navigate the color channel arrays.
					int moveIndex = 1;
					
					// Booleans to confirm that the array has reached one of its ends.
					boolean stepForwardRed = false;
					boolean stepBackwardRed = false;
					boolean stepForwardGreen = false;
					boolean stepBackwardGreen = false;
					boolean stepForwardBlue = false;
					boolean stepBackwardBlue = false;
					
					// Compare pixel color values to beads in palette until the closest match is identified.
					while(!stopSearching) {
						// Add new bead color to red channel. Check to see if this bead is found in all channels.
						if(masterMatch.addBeadAsPossibleMatch(colorMap, redChannelArray.get(redIndex).getCode(), 0) == true) {
							beadGrid[i][j] = new Bead(redChannelArray.get(redIndex).getName(), redChannelArray.get(redIndex).getCode(), redChannelArray.get(redIndex).getColor(), i, j);
							if(colorList.get(redChannelArray.get(redIndex).getName()) == null) {
								colorList.put(redChannelArray.get(redIndex).getName(), 1);
							} else {
								int temp = colorList.get(redChannelArray.get(redIndex).getName());
								colorList.put(redChannelArray.get(redIndex).getName(), ++temp);
							}
							stopSearching = true;
							break;
						}
						// Add new bead color to green channel. Check to see if this bead is found in all channels.
						if(masterMatch.addBeadAsPossibleMatch(colorMap, greenChannelArray.get(greenIndex).getCode(), 1) == true) {
							beadGrid[i][j] = new Bead(greenChannelArray.get(greenIndex).getName(), greenChannelArray.get(greenIndex).getCode(), greenChannelArray.get(greenIndex).getColor(), i, j);
							if(colorList.get(greenChannelArray.get(greenIndex).getName()) == null) {
								colorList.put(greenChannelArray.get(greenIndex).getName(), 1);
							} else {
								int temp = colorList.get(greenChannelArray.get(greenIndex).getName());
								colorList.put(greenChannelArray.get(greenIndex).getName(), ++temp);
							}
							stopSearching = true;
							break;
						}
						// Add new bead color to blue channel. Check to see if this bead is found in all channels.
						if(masterMatch.addBeadAsPossibleMatch(colorMap, blueChannelArray.get(blueIndex).getCode(), 2) == true) {
							beadGrid[i][j] = new Bead(blueChannelArray.get(blueIndex).getName(), blueChannelArray.get(blueIndex).getCode(), blueChannelArray.get(blueIndex).getColor(), i, j);
							if(colorList.get(blueChannelArray.get(blueIndex).getName()) == null) {
								colorList.put(blueChannelArray.get(blueIndex).getName(), 1);
							} else {
								int temp = colorList.get(blueChannelArray.get(blueIndex).getName());
								colorList.put(blueChannelArray.get(blueIndex).getName(), ++temp);
							}
							stopSearching = true;
							break;
						} 
						// If a color match has not been confirmed, move the index on each color channel array.
						else {
							moveIndex *= -1;
							if(moveIndex > 0) {
								moveIndex++;
							} else {
								moveIndex--;
							}
							
							// Navigate red channel array
							if(stepForwardRed) {
								redIndex++;
							} else if(stepBackwardRed) {
								redIndex--;
							} else if(redIndex + moveIndex > redChannelArray.size() -1 && !stepForwardRed && !stepBackwardRed) {
								redIndex--;
								stepBackwardRed = true;
							} else if(redIndex + moveIndex < 0 && !stepForwardRed && !stepBackwardRed) {
								redIndex++;
								stepForwardRed = true;
							} else {
								redIndex += moveIndex;
							}

							// Navigate green channel array
							if(stepForwardGreen) {
								greenIndex++;
							} else if(stepBackwardGreen) {
								greenIndex--;
							} else if(greenIndex + moveIndex > greenChannelArray.size() - 1 && !stepForwardGreen && !stepBackwardGreen) {
								greenIndex--;
								stepBackwardGreen = true;
							} else if(greenIndex + moveIndex < 0 && !stepForwardGreen && !stepBackwardGreen) {
								greenIndex++;
								stepForwardGreen = true;
							} else {
								greenIndex += moveIndex;
							}
							
							// Navigate blue channel array
							if(stepForwardBlue) {
								blueIndex++;
							} else if(stepBackwardBlue) {
								blueIndex--;
							} else if(blueIndex + moveIndex > blueChannelArray.size() - 1 && !stepForwardBlue && !stepBackwardBlue) {
								blueIndex--;
								stepBackwardBlue = true;
							} else if(blueIndex + moveIndex < 0 && !stepForwardBlue && !stepBackwardBlue) {
								blueIndex++;
								stepForwardBlue = true;
							} else {
								blueIndex += moveIndex;
							}
						}
					}
					//System.out.println(beadGrid[i][j].getName());
					//System.out.println("R: " + beadGrid[i][j].getColor().getRed() + " G: " + beadGrid[i][j].getColor().getGreen() + " B: " + beadGrid[i][j].getColor().getBlue() + '\n');
				}
			}
			// After color match has been found for all pixels, create a BeadMatrix with all color information and return it.
			BeadMatrix bm = new BeadMatrix(beadGrid, colorList);
			return bm;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}

/**
 * This class is used to help determine the closest matching bead color for each pixel. Each ColorMatch
 * object represents a single color as defined by its code, and a list of booleans to track whether it 
 * has been identified as a possible match in a given color channel.
 */
class ColorMatch {
	// The color code of a bead.
	private String code;
	
	// Has this bead color been identified as a possible match in the Red, Green, or Blue channel.
	private boolean[] channels = {false, false, false};
	
	/** Default constructor.*/
	public ColorMatch () {
	}
	
	/**
	 * Constructor accepting color code.
	 * @param code Color code for bead object.
	 */
	public ColorMatch (String code) {
		this.code = code;
	}
	
	/**
	 * To identify the closest matching bead color for each pixel, each color channel submits its closest matching
	 * bead as determined by RGB value. When a new bead color is submitted, it is added to a map of ColorMatch objects. 
	 * This method first looks to see if the color is already in the map. If not, it is added to the map. It then 
	 * calls the addToChannel method to confirm that the color has been identified as a possible match in this channel. 
	 * It then calls the isMatch method to determine if the color has been identified as a possible match on all three 
	 * channels. If so, this bead color is the closest match for the pixel. It therefore returns true.
	 * @param map A map of ColorMatch objects, keyed by color code.
	 * @param key The color code of the bead.
	 * @param channel The current channel being tested.
	 * @return Returns true if the bead color has been identified on all three channels as a possible match.
	 */
	public boolean addBeadAsPossibleMatch (Map map, String key, int channel) {
		if(map.containsKey(key)) {
			addToChannel((ColorMatch)map.get(key), channel);
		} else {
			map.put(key, new ColorMatch(key));
			addToChannel((ColorMatch)map.get(key), channel);
		}
		if(isMatch((ColorMatch)map.get(key))) {
			//System.out.println("Match");
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Sets the boolean value of the ColorMatch object to true for the given channel. This means that this particular 
	 * bead color has been identified as a possible match for the current pixel.
	 * @param cm A ColorMatch object.
	 * @param channel A color channel (0 = R, 1 = G, 2 = B)
	 */
	public void addToChannel(ColorMatch cm, int channel) {
		cm.channels[channel] = true;
	}
	
	/**
	 * Examines a ColorMatch object to see if this color has been identified as a possible match in all three 
	 * color channels.
	 * @param cm A ColorMatch object.
	 * @return Returns true if each boolean in channels property has been set to true.
	 */
	public boolean isMatch(ColorMatch cm) {
		if(cm.channels[0] == true && cm.channels[1] == true && cm.channels[2] == true) {
			return true;
		} else {
			return false;
		}
	}
}

/**
 * Contains a grid of bead objects listed by rows, along with a map that includes the color name of each bead used in 
 * the pattern and the quantity of each bead color. 
 */
class BeadMatrix {
	public Bead[][] beadGrid = new Bead[0][0];
	public Map<String, Integer> colorList = new HashMap<>();
	
	/**
	 * Constructor for BeadMatrix.
	 * @param grid Contains a grid of bead objects listed by rows. Mimics an image.
	 * @param map Contains a key of every color of bead included in the grid and a value of how many beads of that color are used in the grid.
	 */
	public BeadMatrix(Bead[][] grid, Map<String, Integer> map) {
		this.beadGrid = grid;
		this.colorList.putAll(map);
	}
}

/**
 * A digital representation of a physical Perler bead. Each bead has a name, a color code, a color, and can also be given an x and y coordinate.
 */
class Bead {
	private String name;
	private String code;
	private Color color;
	private int x;
	private int y;
	
	public Bead(String name, String code, Color color) {
		this.name = name;
		this.code = code;
		this.color = color;
	}
	
	public Bead(String name, String code, Color color, int y, int x) {
		this.name = name;
		this.code = code;
		this.color = color;
		this.x = x;
		this.y = y;
	}
	
	public String getName() {
		return name;
	}
	
	public String getCode() {
		return code;
	}
	
	public Color getColor() {
		return color;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}
}

/**
 * Creates a library of bead colors. Creates bead objects mimicking the true names and colors of Perler beads. 
 * Includes methods to populate color palettes.
 */
class ColorLibrary {
	// Define each bead color with name, code, and RGB color value.
	Bead white = new Bead("White", "01", new Color(241, 241, 241));	
	Bead cream = new Bead("Cream", "02", new Color(224, 222, 169));
	Bead yellow = new Bead("Yellow", "03", new Color(236, 216, 0));
	Bead orange = new Bead("Orange", "04", new Color(237, 97, 32));
	Bead red = new Bead("Red", "05", new Color(191, 46, 64));
	Bead bubblegum = new Bead("Bubblegum", "06", new Color(221, 102, 155));
	Bead purple = new Bead("Purple", "07", new Color(96, 64, 137));
	Bead darkBlue = new Bead("Dark Blue", "08",	new Color(43, 63, 135));
	Bead lightBlue = new Bead("Light Blue", "09", new Color(51, 112, 192));
	Bead darkGreen = new Bead("Dark Green", "10", new Color(28, 117, 62));
	Bead pearlCoral = new Bead("Pearl Coral", "100", new Color(249, 126, 121));
	Bead pearlLightBlue = new Bead("Pearl Light Blue", "101", new Color(122, 174, 162));
	Bead pearlGreen = new Bead("Pearl Green", "102", new Color(132, 183, 145));
	Bead pearlYellow = new Bead("Pearl Yellow", "103", new Color(202, 192, 51));
	Bead pearlLightPink = new Bead("Pearl Light Pink", "104", new Color(215, 168, 162));
	Bead silver = new Bead("Silver", "105", new Color(119, 123, 129));
	Bead lightGreen = new Bead("Light Green", "11", new Color(86, 186, 159));
	Bead brown = new Bead("Brown", "12", new Color(81, 57, 49));
	Bead grey = new Bead("Grey", "17", new Color(138, 141, 145));
	Bead black = new Bead("Black", "18", new Color(46, 47, 50));
	Bead rust = new Bead("Rust", "20", new Color(140, 55, 44));
	Bead lightBrown = new Bead("Light Brown", "21", new Color(129, 93, 52));
	Bead peach = new Bead("Peach", "33", new Color(238, 186, 178));
	Bead tan = new Bead("Tan", "35", new Color(188, 147, 113));
	Bead magenta = new Bead("Magenta", "38", new Color(242, 42, 123));
	Bead neonYellow = new Bead("Neon Yellow", "47", new Color(220, 224, 2));
	Bead neonOrange = new Bead("Neon Orange", "48", new Color(255, 119, 0));
	Bead neonGreen = new Bead("Neon Green", "49", new Color(1, 158, 67));
	Bead neonPink = new Bead("Neon Pink", "50", new Color(255, 57, 145));
	Bead pastelBlue = new Bead("Pastel Blue", "52", new Color(83, 144, 209));
	Bead pastelGreen = new Bead("Pastel Green", "53", new Color(118, 200, 130));
	Bead pastelLavender = new Bead("Pastel Lavender", "54", new Color(138, 114, 193));
	Bead pastelYellow = new Bead("Pastel Yellow", "56", new Color(254, 248, 117));
	Bead cheddar = new Bead("Cheddar", "57", new Color(241, 170, 12));
	Bead toothpaste = new Bead("Toothpaste", "58", new Color(147, 200, 212));
	Bead hotCoral = new Bead("Hot Coral", "59", new Color(255, 56, 81));
	Bead plum = new Bead("Plum", "60", new Color(62, 75, 156));
	Bead kiwiLime = new Bead("Kiwi Lime", "61", new Color(108, 190, 19));
	Bead cyan = new Bead("Cyan", "62", new Color(43, 137, 198));
	Bead blush = new Bead("Blush", "63", new Color(255, 130, 133));
	Bead periwinkleBlue = new Bead("Periwinkle Blue", "70", new Color(100, 124, 190));
	Bead lightPink = new Bead("Light Pink", "79", new Color(246, 179, 221));
	Bead brightGreen = new Bead("Bright Green", "80", new Color(79, 173, 66));
	Bead lightGray = new Bead("Light Gray", "81", new Color(177, 181, 178));
	Bead evergreen = new Bead("Evergreen", "179", new Color(53, 83, 67));
	Bead lavender = new Bead("Lavender", "82", new Color(173, 152, 212));
	Bead pink = new Bead("Pink", "83", new Color(228, 72, 146));
	Bead gold = new Bead("Gold", "85", new Color(187, 118, 52));
	Bead raspberry = new Bead("Raspberry", "88", new Color(165, 48, 97));
	Bead butterscotch = new Bead("Butterscotch", "90", new Color(212, 132, 55));
	Bead parrotGreen = new Bead("Parrot Green", "91", new Color(6, 124, 129));
	Bead darkGrey = new Bead("Dark Grey", "92", new Color(77, 81, 86));
	Bead blueberryCream = new Bead("Blueberry Cream", "93", new Color(130, 151, 217));
	
	/**
	 * Defines the color palette to be used in the process of converting pixel colors to bead colors. This method
	 * uses all possible colors.
	 * @param array An ArrayList that contains the color palette.
	 */
	public void populateArrayAllColors(ArrayList<Bead> array) {
		array.add(white);
		array.add(cream);
		array.add(yellow);
		array.add(orange);
		array.add(red);
		array.add(bubblegum);
		array.add(purple);
		array.add(darkBlue);
		array.add(lightBlue);
		array.add(darkGreen);
		array.add(pearlCoral);
		array.add(pearlLightBlue);
		array.add(pearlGreen);
		array.add(pearlYellow);
		array.add(pearlLightPink);
		array.add(silver);
		array.add(lightGreen);
		array.add(brown);
		array.add(grey);
		array.add(black);
		array.add(rust);
		array.add(lightBrown);
		array.add(peach);
		array.add(tan);
		array.add(magenta);
		array.add(neonYellow);
		array.add(neonOrange);
		array.add(neonGreen);
		array.add(neonPink);
		array.add(pastelBlue);
		array.add(pastelGreen);
		array.add(pastelLavender);
		array.add(pastelYellow);
		array.add(cheddar);
		array.add(toothpaste);
		array.add(hotCoral);
		array.add(plum);
		array.add(kiwiLime);
		array.add(cyan);
		array.add(blush);
		array.add(periwinkleBlue);
		array.add(lightPink);
		array.add(brightGreen);
		array.add(lightGray);
		array.add(evergreen);
		array.add(lavender);
		array.add(pink);
		array.add(gold);
		array.add(raspberry);
		array.add(butterscotch);
		array.add(parrotGreen);
		array.add(darkGrey);
		array.add(blueberryCream);
	}
	
	/**
	 * Defines the color palette to be used in the process of converting pixel colors to bead colors. This method
	 * uses a palette of colors found in a commonly used set of Perler beads.
	 * @param array An ArrayList that contains the color palette.
	 */
	public void populateArrayFunColors(ArrayList<Bead> array) {
			array.add(pink);
			array.add(magenta);
			array.add(red);
			array.add(cheddar);
			array.add(neonOrange);
			array.add(yellow);
			array.add(lightGreen);
			array.add(kiwiLime);
			array.add(parrotGreen);
			array.add(lightBlue);
			array.add(plum);
			array.add(pink);
			array.add(purple);
			array.add(lightBrown);
			array.add(black);
			array.add(white);
	}
	
	/**
	 * Reads all the bead colors contained in the given array, and creates a map storing the name and code of 
	 * each color. This map is used to create a list on the final printable file that includes the color code 
	 * for each bead color.
	 * @param array An array of bead objects that defines the color palette of the project.
	 * @return Returns a map containing color names as keys and color codes as values.
	 */
	public Map<String,String> createCodeMap(ArrayList<Bead> array) {
		Map<String,String> codeMap = new HashMap<>();
		for(int i = 0; i < array.size(); i++) {
			codeMap.put(array.get(i).getName(), array.get(i).getCode());
		}
		return codeMap;
	}
}

/**
 * Used to compare the averaged tonal value of a bead object. Not currently used, but may prove useful in future updates.
 */
class TonalValueComparator implements Comparator<Bead>, java.io.Serializable {
	public int compare(Bead b1, Bead b2) {
		int rgb1 = ((b1.getColor().getRed() + b1.getColor().getGreen() + b1.getColor().getBlue()) / 3);
		int rgb2 = ((b2.getColor().getRed() + b2.getColor().getGreen() + b2.getColor().getBlue()) / 3);
		
		if (rgb1 < rgb2) {
			return -1;
		} else if (rgb1 == rgb2) {
			return 0;
		} else {
			return 1;
		}
	}
}

/**
 * Used to compare the red channel value of a bead object.
 */
class RedChannelComparator implements Comparator<Bead>, java.io.Serializable {
	public int compare(Bead b1, Bead b2) {
		int r1 = b1.getColor().getRed();
		int r2 = b2.getColor().getRed();
		
		if (r1 < r2) {
			return -1;
		} else if (r1 == r2) {
			return 0;
		} else {
			return 1;
		}
	}
}

/**
 * Used to compare the green channel value of a bead object.
 */
class GreenChannelComparator implements Comparator<Bead>, java.io.Serializable {
	public int compare(Bead b1, Bead b2) {
		int g1 = b1.getColor().getGreen();
		int g2 = b2.getColor().getGreen();
		
		if (g1 < g2) {
			return -1;
		} else if (g1 == g2) {
			return 0;
		} else {
			return 1;
		}
	}
}

/**
 * Used to compare the blue channel value of a bead object.
 */
class BlueChannelComparator implements Comparator<Bead>, java.io.Serializable {
	public int compare(Bead b1, Bead b2) {
		int rgb1 = b1.getColor().getBlue();
		int rgb2 = b2.getColor().getBlue();
		
		if (rgb1 < rgb2) {
			return -1;
		} else if (rgb1 == rgb2) {
			return 0;
		} else {
			return 1;
		}
	}
}

/**
 * Creates and exports an image file ready to be printed on letter sized paper at 300dpi.
 */
class CreateImageAndExportToFile {
	// Width and height of page, and radius of each bead. 
	final int PAGE_WIDTH = 2550;
	final int PAGE_HEIGHT = 3300;
	final int RADIUS = 29;
	final int DIAMETER = RADIUS * 2;
	int borderWidth = 3;
	// Starting x and y coordinates for list of beads.
	int listX = 200;
	int listY = 2200;
	int listCounter = 0;
	
	/**
	 * This method formats and exports the output file. It creates a white page, paints rows of beads in their 
	 * appropriate colors overlaid with their color codes, adds title text, and creates a list of bead colors 
	 * needed to complete the project as well as the quantity in which they are needed.
	 * @param fileName The name of the image file originally imported for conversion.
	 * @param matrix The BeadMatrix containing the grid of bead objects and list of bead colors used.
	 * @param codeMap A map containing color names and their color codes.
	 */
	public CreateImageAndExportToFile(String fileName, BeadMatrix matrix, Map<String,String> codeMap) {
		// Create BufferedImage to hold graphics.
		BufferedImage bufferedImage = new BufferedImage(PAGE_WIDTH, PAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
		
		// Create white page.
		Graphics2D page = bufferedImage.createGraphics();
		page.setColor(Color.WHITE);
		page.fillRect(0, 0, PAGE_WIDTH, PAGE_HEIGHT);
		page.dispose();
		
		// Set width and height of grid. Set x and y location of grid.
		int width = DIAMETER * matrix.beadGrid[0].length;
		int height = DIAMETER * matrix.beadGrid.length;
		int startX = (PAGE_WIDTH - width) / 2;
		int startY = 250;
		
		// Create grid and set font for code.
		Graphics2D pegboard = bufferedImage.createGraphics();
		Font codeFont = new Font("Helvetica", 1, (DIAMETER / 3));
		pegboard.setFont(codeFont);
				
		// Create colored circle for each bead and add code.
		pegboard.setColor(Color.BLACK);
		for(int i = 0; i < matrix.beadGrid.length; i++) {
			for(int j = 0; j < matrix.beadGrid[i].length; j++) {
				pegboard.fillOval((DIAMETER * j) + startX, (DIAMETER * i) + startY, DIAMETER, DIAMETER);
				pegboard.setColor(matrix.beadGrid[i][j].getColor());
				pegboard.fillOval(((DIAMETER * j) + borderWidth) + startX, ((DIAMETER * i) + borderWidth) + startY, DIAMETER - (2 * borderWidth), DIAMETER - (2 * borderWidth));
				pegboard.setColor(Color.BLACK);
				pegboard.drawString(matrix.beadGrid[i][j].getCode(), ((DIAMETER * j) + RADIUS - (RADIUS / 3)) + startX, ((DIAMETER * i) + RADIUS + (RADIUS / 6)) + startY);
			}
		} 
		pegboard.dispose();
		
		// Create list of bead colors with their color code and number of beads required for project.
		Graphics2D beadList = bufferedImage.createGraphics();
		Font listFont = new Font("Helvetica", 1, 30);
		beadList.setFont(listFont);
		beadList.setColor(Color.BLACK);
		beadList.drawString("Perler Bead Pattern for " + fileName, 1050, 150);
		beadList.drawString("You will need the following bead colors:", listX, listY - 100);
		
		// Draw each item of list.
		matrix.colorList.forEach((k, v) -> {
			beadList.drawString(k + " (" + codeMap.get(k) + ") - " + v + " beads", listX, listY);
			listY += 100;
			listCounter++;
			if(listCounter % 10 == 0) {
				listY = 2200;
				listX += 600;
			}
		});
		beadList.dispose();
			
		try {
			// Save as PNG
			File file = new File("BeadTemplate.png");
			ImageIO.write(bufferedImage, "png", file);
		} catch (IOException ex) {
			System.out.print(ex);
		}
	} 
}