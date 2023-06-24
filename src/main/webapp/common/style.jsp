<style>
body {
  box-sizing: border-box;
  width: 100%;
  height: calc(100vh - 24px);
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

#header-right {
  position: absolute;
  right: 4px;
}

#clock {
  margin-left: 1em;
  font-family: Consolas, Monaco, Menlo, monospace, sans-serif;
}

#version {
  margin-left: .5em;
}

</style>
