### 0.1.1 (2019-Jan-09)

Request to fetch plugin_settings uses version 1.0:
  * The API version used for request processors was same as the Plugin API
    version. In 19.1.0, GoCD core is changed to delink the Plugin API
    version from request processor version - gocd/gocd#5654.
  * Ensuring we use the right version of the message when making a call to
    a request process

### 0.1.0 (2018-Sep-04)

Initial release
