<style>
body {
  box-sizing: border-box;
  width: 100%;
  min-width: 1000px;
  height: calc(100vh - 10px);
  margin: 0;
  padding: 0;
  margin: 0;
  padding: 0;
  font-size: 14px;
  font-family: Consolas, Monaco, Menlo, monospace, sans-serif;
}

button, input[type="button"], input[type="submit"] {
  width: 70px;
  height: 20px;
  border: none;
  border-radius: 2px;
  outline: none;
  color: #fff;
  background: #0068cc;
  transition: all 0.2s ease;
}

button:hover, input[type="button"]:hover, input[type="submit"]:hover {
  cursor:pointer;
  background: #00a8ff;
  color: #fff;
  transition: all 0.2s ease;
}

input[type="text"],input[type="password"] {
  border: none;
  border-bottom: 1px solid #000;
  outline: none;
  font-family: Consolas, Monaco, Menlo, monospace, sans-serif;
}

input[type="checkbox"] {
  position: relative;
  top: 2px;
}

pre {
  margin: 2px 0;
  font-family: Consolas, Monaco, Menlo, monospace, sans-serif;
}

table {
  border-collapse: collapse;
}

td,th {
  padding: 2px 4px;
}

.screen-button {
  min-width: 7em;
}

.screen-button-active {
  font-weight: bold;
}

.sort-button {
  display: inline-block;
  line-height: 1em;
  color: #ccc;
  font-size: 8px;
}

.sort-button:hover {
  cursor: pointer;
}

.sort-active {
  color: #555;
}

.dialog-content {
  font-size: 18px;
}

#clock {
  margin-left: 1em;
  font-family: Consolas, Monaco, Menlo, monospace, sans-serif;
}

#header {
  display: table;
  position: relative;
  width: 100%;
  height: 46px;
  table-layout: fixed;
  white-space: nowrap;
  color: #fff;
  background: #01a0e9;
}

#header-content {
  display: table-cell;
  position: relative;
  width: 100%;
  height: 100%;
  padding: 0 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: middle;
}

#contents {
  width: calc(100% - 8px);
  position: relative;
  min-height: calc(100% - 92px);
  padding: 4px;
  overflow: auto;
}

#footer {
  display: table;
  position: relative;
  width: 100%;
  height: 46px;
  table-layout: fixed;
  white-space: nowrap;
  color: #8b949d;
  background: #0d1117;
}

#footer-content {
  display: table-cell;
  position: relative;
  width: 100%;
  height: 100%;
  padding: 0 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: middle;
}

</style>
