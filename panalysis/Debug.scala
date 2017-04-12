package panalysis {

object Debug{

  var enabled = false

  def enable = {
    this.enabled = true
  }

  def disable = {
    this.enabled = false
  }

  def message(msg: String, messageType: String = "DEBUG: ") = {
    if (this.enabled) {
      Utils.message(msg, messageType)
    }
  }

  def warning(msg: String) = message(msg, "DEBUG-WARNING: ")
  def error(msg: String) = message(msg, "DEBUG-ERROR: ")
    

  }

}
