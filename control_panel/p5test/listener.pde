class TestListener implements OscEventListener {

  public void oscEvent(OscMessage msg) {
    if (msg.addrPattern().equals("/sys/subscribed")) {
      println("subscribed successfully");
    } else if (msg.addrPattern().equals("/sys/configured")) {
      println("successfully configured ?");
    } else if (msg.addrPattern().equals("/slider")) {
      band = (int)msg.get(0).floatValue();
      val = msg.get(1).floatValue();
    } else {
      println("unexpected message received " + msg.addrPattern());
    }
  }

  public void oscStatus(OscStatus theStatus) {
    println("osc status : "+theStatus.id());
  }
}