(function(){
  window.Controller = function(data,instanceName,paletteId,widthFieldId,heightFieldId,canvasId,dataFields) {
    var me = this;
    var coeff = 25;
    var updateSaveData = function() {
      dataFields.forEach(function(id){
          document.getElementById(id).value = JSON.stringify(data);
      });
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
        data.palette.forEach(function(color, index) {
          while(document.getElementById("color" + index) === null) {
            me.addColor();
          }
          document.getElementById("color" + index).value = color;
        });
        buildCanvas(data.grid);
        updateSaveData();
    };
    this.addColor = function() {
      data.palette.push("white");
      build(document.getElementById(paletteId),data.palette.filter(function(c,i){return i>0;}).map(function(color,n){
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
              type: "color",
              id: "color" + index,
              value: color,
              onChange: instanceName + ".updateColor(" + index + ")"
            }
          }]
        };
      }));
      buildCanvas(data.grid);
    };
    this.updateColor = function(index) {
      var colorField = document.getElementById("color" + index);
      var colorObj = document.getElementById("r" + index);
      var fill = colorObj.attributes.getNamedItem("fill");
      fill.value = colorField.value;
      data.palette[index] = colorField.value;
      updateSaveData();
    }
    this.resize = function() {
      data.width = parseInt(document.getElementById(widthFieldId).value);
      data.height = parseInt(document.getElementById(heightFieldId).value);
      buildCanvas(data.grid);
      updateSaveData();
    };
    this.setColor = function(x, y) {
      var colorIndex = data.palette.map(function(c,n){
        return document.getElementById("colorSelect" + n);
      }).filter(function(radio,n){
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
    this.transform = function(type) {
        var tf = buildTypes(data.width, data.height)[type];
        var newGrid = {};
        Object.keys(data.grid).map(function(id){
          var point = id.split("-").map(parseInt);
          return {
            x: tf.x.apply(tf, point),
            y: tf.y.apply(tf, point),
            color: data.grid[id]
          }
        }).filter(function(obj){
          return (obj.x < data.width) && (obj.y < data.height);
        }).forEach(function(obj){
          var id = obj.x + "-" + obj.y;
          newGrid[id] = obj.color;
        });
        data.grid = newGrid;
        buildCanvas(data.grid);
    }
  };
})()
