package codelet;

import codeletManager.CodeletManager;

import java.util.ArrayList;

/**
 * Created by kassip on 6/22/2015.
 * The abstract Codelet class. A java Codelet can directly extend this and put its operations in
 * its definition of run. An external executable codelet should be instantiated in the ExeCodelet subclass.
 */

public abstract class Codelet implements Runnable {
  protected final ArrayList<String> TAGS = new ArrayList<>(); //informational tags for the Codelet, such as deductive, inductive, parsing, simplifying, etc...
  protected final CodeletManager MAN;
  protected String name;
  protected long running = 0;

  //
  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o != null && toString() == o.toString() && getClass() == o.getClass()) {
      return true;
    }
    return false;
  }

  /**
   * Constructor for the Codelet class
   *
   * @param man, the CodeletManager that is running this codelets
   */
  public Codelet(CodeletManager man, String name) {
    MAN = man;
    this.name = name;
  }

  /**
   * Calculates how long the codelet has been running.
   *
   * @return a long representing how many milliseconds the codelet has been running.
   */
  public long runtime() {
    if (running == 0) return 0;
    return System.currentTimeMillis() - running;
  }

  public abstract void kill();

  /**
   * Returns a copy of the tag list
   *
   * @return the tag list
   */
  public ArrayList<String> getTAGS() {
    return new ArrayList<String>(TAGS);
  }

  public String toString() {
    return name;
  }
}