(function(){
  window.Controller = function(data,instanceName,paletteId,widthFieldId,heightFieldId,canvasId,saveDataId) {
    var defaultColors = ["red","blue","green"];
    var coeff = 25;
    var updateSaveData = function() {
      document.getElementById(saveDataId).value = JSON.stringify(data);
    }
    var buildCanvas = function(grid) {
      build(document.getElementById(canvasId),[{
        tag: "svg",
        attrs: {
          width: "50%",
          height: "50%",
          viewBox: [0,0,(coeff * data.width),(coeff * data.height)].join(" ")
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
          }].concat(data.palette.map(function(color, n){
            if (color === "") {
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
        }].concat(range(data.height).reduce(function(out,y){
          return out.concat(range(data.width).map(function(x){
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
    };
    this.init = function() {
        document.getElementById(widthFieldId).value = data.width;
        document.getElementById(heightFieldId).value = data.height;
        while(colorCount < data.palette.length) {
          this.addColor();
        }
        data.palette.forEach(function(color, index) {
          document.getElementById("color" + index).value = color;
        });
        buildCanvas(data.grid);
        updateSaveData();
    };
    this.addColor = function() {
      data.palette.push("white");
      build(document.getElementById(paletteId),data.palette.map(function(color,n){
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
      }));
      buildCanvas(data.grid);
    };
    var updateColor = function(index) {
      var colorField = document.getElementById("color" + index);
      var colorObj = document.getElementById("r" + index);
      var fill = colorObj.attributes.getNamedItem("fill");
      fill.value = colorField.value;
      data.color[n] = colorField.value;
      updateSaveData();
    }
    this.resize = function() {
      data.width = document.getElementById(widthFieldId).value;
      data.height = document.getElementById(heightFieldId).value;
      buildCanvas(data.grid);
      updateSaveData();
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
      href.value = "#r" + colorIndex;
      if (colorIndex > 0) {
          data.grid[id] = colorIndex;
      } else {
        delete data.grid[id];
      }
      updateSaveData();
    }
  };
})()
