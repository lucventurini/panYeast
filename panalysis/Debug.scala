package panalysis {

object Debug {

  var enabled = false

  def enable = {
    this.enabled = true
  }

  def disable = {
    this.enabled = false
  }

  def message(msg: String, messageType: String = "DEBUG: ", ln: Boolean = true) = {
    if (this.enabled) {
      Utils.message(msg, messageType, ln=ln, or=true)
    }
  }

  def warning(msg: String, ln: Boolean = true) = message(msg, "DEBUG-WARNING: ", ln=ln)
  def error(msg: String, ln: Boolean = true) = message(msg, "DEBUG-ERROR: ", ln=ln)
    

  }

}
