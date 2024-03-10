package be.ugent.benchmark;

import java.util.concurrent.ConcurrentHashMap;

public class SolutionReporter<T> {
  private final ConcurrentHashMap<String, Solution<T>> solutions = new ConcurrentHashMap<>();

  /**
   * Report a solution to the solution reporter This method is thread safe and can be called from
   * multiple threads at the same time. However, it will <b>not</b> check whether the solution is
   * better than the current set solution, this is left to the calling algorithm.
   *
   * @param solution The Object representing the solution
   * @param iterationId The id of the iteration for which the solution is reported
   * @param isFinal Whether the solution is final or not
   */
  public void reportSolution(T solution, String iterationId, boolean isFinal) {
    Solution<T> currentSolution = solutions.getOrDefault(iterationId, new Solution<>(null));
    currentSolution.setValue(solution);
    currentSolution.setFinal(isFinal);
    solutions.put(iterationId, currentSolution);
  }

  /**
   * Report a non-final solution to the solution reporter. For more information, see {@link
   * SolutionReporter#reportSolution(T, String, boolean)}
   *
   * @param solution The representation the solution
   * @param iterationId The id of the iteration for which the solution is reported
   */
  public void reportSolution(T solution, String iterationId) {
    reportSolution(solution, iterationId, false);
  }

  /**
   * Get the solution for a given iteration
   *
   * @param iterationId The id of the iteration for which the solution is requested
   * @return The solution for the given iteration
   */
  public Solution<T> getSolution(String iterationId) {
    return solutions.get(iterationId);
  }
}
