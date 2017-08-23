(function(){
  var colors = {
    "AliceBlue ": true,
    "AntiqueWhite ": true,
    "Aqua ": true,
    "Aquamarine ": true,
    "Azure ": true,
    "Beige ": true,
    "Bisque ": true,
    "Black ": false,
    "BlanchedAlmond ": true,
    "Blue ": false,
    "BlueViolet ": false,
    "Brown ": false,
    "BurlyWood ": true,
    "CadetBlue ": true,
    "Chartreuse ": true,
    "Chocolate ": false,
    "Coral ": true,
    "CornflowerBlue ": true,
    "Cornsilk ": true,
    "Crimson ": false,
    "Cyan ": true,
    "DarkBlue ": false,
    "DarkCyan ": false,
    "DarkGoldenRod ": false,
    "DarkGray ": false,
    "DarkGrey ": false,
    "DarkGreen ": false,
    "DarkKhaki ": true,
    "DarkMagenta ": false,
    "DarkOliveGreen ": false,
    "DarkOrange ": true,
    "DarkOrchid ": false,
    "DarkRed ": false,
    "DarkSalmon ": true,
    "DarkSeaGreen ": true,
    "DarkSlateBlue ": false,
    "DarkSlateGray ": false,
    "DarkSlateGrey ": false,
    "DarkTurquoise ": true,
    "DarkViolet ": false,
    "DeepPink ": false,
    "DeepSkyBlue ": true,
    "DimGray ": false,
    "DimGrey ": false,
    "DodgerBlue ": true,
    "FireBrick ": false,
    "FloralWhite ": true,
    "ForestGreen ": false,
    "Fuchsia ": false,
    "Gainsboro ": true,
    "GhostWhite ": true,
    "Gold ": true,
    "GoldenRod ": true,
    "Gray ": false,
    "Grey ": false,
    "Green ": false,
    "GreenYellow ": true,
    "HoneyDew ": true,
    "HotPink ": true,
    "IndianRed  ": false,
    "Indigo  ": false,
    "Ivory ": true,
    "Khaki ": true,
    "Lavender ": true,
    "LavenderBlush ": true,
    "LawnGreen ": true,
    "LemonChiffon ": true,
    "LightBlue ": true,
    "LightCoral ": true,
    "LightCyan ": true,
    "LightGoldenRodYellow ": true,
    "LightGray ": true,
    "LightGrey ": true,
    "LightGreen ": true,
    "LightPink ": true,
    "LightSalmon ": true,
    "LightSeaGreen ": true,
    "LightSkyBlue ": true,
    "LightSlateGray ": false,
    "LightSlateGrey ": false,
    "LightSteelBlue ": true,
    "LightYellow ": true,
    "Lime ": true,
    "LimeGreen ": true,
    "Linen ": true,
    "Magenta ": false,
    "Maroon ": false,
    "MediumAquaMarine ": true,
    "MediumBlue ": false,
    "MediumOrchid ": true,
    "MediumPurple ": true,
    "MediumSeaGreen ": true,
    "MediumSlateBlue ": true,
    "MediumSpringGreen ": true,
    "MediumTurquoise ": true,
    "MediumVioletRed ": false,
    "MidnightBlue ": false,
    "MintCream ": true,
    "MistyRose ": true,
    "Moccasin ": true,
    "NavajoWhite ": true,
    "Navy ": false,
    "OldLace ": true,
    "Olive ": false,
    "OliveDrab ": false,
    "Orange ": true,
    "OrangeRed ": true,
    "Orchid ": true,
    "PaleGoldenRod ": true,
    "PaleGreen ": true,
    "PaleTurquoise ": true,
    "PaleVioletRed ": false,
    "PapayaWhip ": true,
    "PeachPuff ": true,
    "Peru ": false,
    "Pink ": true,
    "Plum ": true,
    "PowderBlue ": true,
    "Purple ": false,
    "RebeccaPurple ": false,
    "Red ": false,
    "RosyBrown ": true,
    "RoyalBlue ": true,
    "SaddleBrown ": false,
    "Salmon ": true,
    "SandyBrown ": true,
    "SeaGreen ": false,
    "SeaShell ": true,
    "Sienna ": false,
    "Silver ": true,
    "SkyBlue ": true,
    "SlateBlue ": true,
    "SlateGray ": true,
    "SlateGrey ": true,
    "Snow ": true,
    "SpringGreen ": true,
    "SteelBlue ": false,
    "Tan ": true,
    "Teal ": false,
    "Thistle ": true,
    "Tomato ": true,
    "Turquoise ": true,
    "Violet ": true,
    "Wheat ": true,
    "White ": true,
    "WhiteSmoke ": true,
    "Yellow ": true,
    "YellowGreen ": true
  };
  var Controller = function() {
    this.init = function() {
      build(document.getElementsByTagName("body")[0],[{
        tag: "select",
        attrs: {
          style: "yellow"
        },
        children: Object.keys(colors).map(function(c){
          return {
            tag: "option",
            attrs: {
              value: c,
              style: "color: " + (colors[c]?"black":"white") + "; background-color: " + c
            },
            children: [c]
          };
        })
      },{
        tag: "input",
        attrs: {
          id: "hex",
          type: "text",
          onChange: "ctrl.setColor(this)"
        }
      },{
        tag: "label",
        attrs: {
          for: "hex"
        },
        children: [" White Print? "]
      },{
        tag: "input",
        attrs: {
          id: "hex",
          name: "hex",
          type: "checkbox",
          onChange: "ctrl.setForeColor(this,'hex')"
        }
      }]);
    }
    this.setColor = function(input) {
      console.log(input);
      console.log(input.style);
      console.log(input.value);
      input.style["background-color"] = input.value;
    }
    this.setForeColor = function(input,id) {
      document.getElementById(id).style.color = (input.checked)?"white":"black";
    }
  }
  window.ctrl = new Controller();
})()