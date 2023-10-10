# Paint Release Notes (newest is at top)

Ethan’s Pain(t) Version 1.0.3  - 10/8/23

New features:
- Draws squares, circles, rectangles, ellipses, and triangles
- Thickness controls also work on shapes now
- Keyboard shortcuts to save and close application
- English names for some hex code colors
- Eyedropper tool
- Manual resizing of canvas (width and height customization)
- Added straight lines in addition to freehand drawing
- Added dashed outline capability
- Close without saving warning
- Joke about Java in Help tab

Known issues:
- Hexcode colors only have names for certain ones, not sure how to get names for the rest of the palette
- Canvas resizing loses quality over time
- Drawing shapes and lines after shapes decreases image quality over time
- Drawing lines and shapes erase image
- Can't draw more than one shape at a time

Expected next sprint:
- Undo and Redo - this MUST use at least (more is ok) one "stack" (this is an abstract data structure in Java)
- provide a blank starting image and a clear canvas (with "are you sure" check) tool
- draw an additional shape of your choice
- have an eraser tool
- be able to draw a regular side polygon with any # of sides
- select and copy/paste a piece of the image
- select and move a piece of the image
- allow the addition of text from user-typed input to your image.



Ethan's Pain(t) Version 1.0.2 

New Features:  
- User can now draw a line and change the thickness and color of the line
- Added a help menu
- Added scroll bars to access full image if needed

Known issues: None at the moment. I went through my original program and rebuilt it and everything should work as expected right now. The only thing I'm unsure of how to work with is when maximizing the application, the items that I put in the VBox aren't aligned the way I want them to look. 

Features expected for next version: 
- have width controls  (width controls as in control width of a regular line or the line-edges of shapes)
- draw square, circle, rectangle, ellipse, triangle
- keyboard shortcuts (control S for save, etc)
- text label for colors (hex/rgb/English name)
- color grabber (has to modify shapes)
- resize canvas (for larger drawing)
- pencil (curved) or straight line (whichever you didn't do last time)
- dashed outline of shape / line
- smart/aware save ("you're about to close without saving...")
- include some sort of pun on Java.



Ethan's Pain(t) Version 0.0.1 9/11/23 

New features: 
- Opens image
- Saves image as new file
- Can close/min/max

Known issues: 
- Can't purely save image to itself

Expected next sprint: 
- Everything that is required from "The software shall"
- Cleaner menu bar
- FXML implementation
- Open image button will disappear when new image is loaded (I didn't hard code an image but couldn't figure out why the picture was appearing behind the button)

  
