(function(){
  var Controller = function(instanceName,paletteId,widthFieldId,heightFieldId,canvasId) {
    var defaultColors = ["red","blue","green"];
    var colorCount = 1;
    var dim = {
      width: 10,
      height: 10
    };
    var coeff = 25;
    var readGrid = function() {
      var indexMap = range(colorCount).reduce(function(out,n){
        out["#r" + n] = n;
        return out;
      },{});
      return range(dim.height).reduce(function(grid,y){
        return range(dim.width).reduce(function(out,x){
          var id = x + "-" + y;
          var pixel = document.getElementById(id);
          var color = pixel.attributes.getNamedItem("href").value;
          if (color != "#r0") {
            out[id] = indexMap[color];
          }
          return out;
        },grid);
      },{});
    };
    var buildCanvas = function(grid) {
      build(document.getElementById(canvasId),[{
        tag: "svg",
        attrs: {
          width: "50%",
          height: "50%",
          viewBox: [0,0,(coeff * dim.width),(coeff * dim.height)].join(" ")
        },
        children: [{
          tag: "defs",
          children: [{
            tag: "rect",
            attrs: {
              id: "#r0",
              width: coeff,
              height: coeff,
              stroke: "black",
              "stroke-width": 1,
              fill: "white"
            }
          }].concat(range(colorCount).map(function(n){
            var color = document.getElementById("color"+n).value;
            if (color == "") {
              color = "none";
            }
            return {
              tag: "rect",
              attrs: {
                id: "r" + n,
                width: coeff,
                height: coeff,
                stroke: "black",
                "stroke-width": 1,
                fill: color
              }
            }
          }))
        }].concat(range(dim.height).reduce(function(out,y){
          return out.concat(range(dim.width).map(function(x){
            var id = x + "-" + y;
            var color = (id in grid)?"#r" + grid[id]:"#r0";
            return {
              tag: "use",
              attrs: {
                id: x + "-" + y,
                href: color,
                x: x * coeff,
                y: y * coeff,
                onClick: instanceName + ".setColor(" + x + "," + y + ")"
              }
            }
          }));
        },[]))
      }])
    }
    this.init = function() {
      build(document.getElementsByTagName("body")[0], [{
        tag: "div",
        attrs:{
          id: "ctrls"
        },
        children: [].concat(
        button("Add Color", instanceName + ".addColor()"),
        br(),
        radio("colors","colorSelect0",0,true),
        labeled("backgroundColor"," Background Color: ",text("color0","white",instanceName + ".updateColor(0)")),
        br(),
        {
          tag: "ol",
          attrs: { id: paletteId },
          children: []
        },
        br(),
        labeled("width"," Width: ",number(widthFieldId,dim.width,instanceName + ".resize()")),
        labeled("height"," Height: ",number(heightFieldId,dim.height,instanceName + ".resize()")))
      },
      br(),
      {
        tag: "div",
        attrs: { id: canvasId }
      }]);
      buildCanvas({});
    };
    this.addColor= function() {
      var colorRange = range(colorCount).splice(1);
      var colors = colorRange.map(function(n){
        return document.getElementById("color" + n).value;
      });
      colorCount++;
      colors.push("white");
      build(document.getElementById(paletteId),colors.map(function(color,n){
        var index = n + 1;
        return {
          tag: "li",
          children: [{
            tag: "input",
            attrs: {
              type: "radio",
              name: "colors",
              id: "colorSelect" + index,
              checked: true,
              value: index
            }
          },{
            tag: "input",
            attrs: {
              type: "text",
              id: "color" + index,
              value: color,
              onChange: instanceName + ".updateColor(" + index + ")"
            }
          }]
        };
      }))
      var grid = readGrid();
      buildCanvas(grid);
    };
    var updateColor = function(fieldId,rectId) {
      var colorField = document.getElementById(fieldId);
      var colorObj = document.getElementById(rectId);
      var fill = colorObj.attributes.getNamedItem("fill");
      fill.value = colorField.value;
    }
    this.updateBackground = function() {
      updateColor(bgColorFieldId,"background");
    }
    this.updateColor = function(n) {
      updateColor("color" + n, "r" + n);
    }
    this.resize = function() {
      var grid = readGrid();
      dim.width = document.getElementById(widthFieldId).value;
      dim.height = document.getElementById(heightFieldId).value;
      buildCanvas(grid);
    };
    this.setColor = function(x, y) {
      console.log("set color: (" + x + "," + y + ")");
      var colorIndex = range(colorCount).map(function(n){
        return document.getElementById("colorSelect" + n);
      }).filter(function(radio,n){
        console.log("checking radio item " + n);
        return radio.checked;
      }).map(function(radio){
        return radio.value;
      })[0];
      var color = ((colorIndex) && (colorIndex > 0))?"#r"+colorIndex:"#r0";
      var id = x + "-" + y;
      var pixel = document.getElementById(id);
      var href = pixel.attributes.getNamedItem("href");
      href.value = "#r" + colorIndex
    }
  };
  window.ctrl = new Controller("ctrl","palette","widthField","heightField","canvas");
})()
