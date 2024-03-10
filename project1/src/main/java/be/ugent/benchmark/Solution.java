package be.ugent.benchmark;


/**
 * A class representing a solution to a problem.
 */
public class Solution<T> {
  private T value;
  private boolean isFinal = false;

  /**
   * Returns a string representation of the object.
   *
   * @return a string representation of the object.
   */
  @Override
  public String toString() {
    return value.toString();
  }

  /**
   * Constructs a new Solution object.
   *
   * @param bestSolution The best solution found so far
   */
  public Solution(T bestSolution) {
    this.value = bestSolution;
  }

  /**
   * Returns the best solution found so far.
   *
   * @return the best solution found so far.
   */
  public T getValue() {
    return value;
  }

  /**
   * Sets the best solution found so far.
   *
   * @param bestSolution The best solution found so far
   * @throws IllegalStateException if the solution is final
   */
  public void setValue(T bestSolution) throws IllegalStateException {
    if (!this.isFinal) {
      this.value = bestSolution;
    }
  }

  /**
   * Returns whether the solution is final or not.
   *
   * @return whether the solution is final or not.
   */
  public boolean isFinal() {
    return isFinal;
  }

  /**
   * Sets whether the solution is final or not.
   *
   * @param isFinal whether the solution is final or not
   */
  public void setFinal(boolean isFinal) {
    this.isFinal = isFinal;
  }
}
