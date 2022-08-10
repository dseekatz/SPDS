package boomerang.scene;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import wpds.interfaces.Location;

public interface ControlFlowGraph {

  Collection<Statement> getStartPoints();

  Collection<Statement> getEndPoints();

  Collection<Statement> getSuccsOf(Statement curr);

  Collection<Statement> getPredsOf(Statement curr);

  List<Statement> getStatements();

  class Edge extends Pair<Statement, Statement> implements Location {
    public Edge(Statement start, Statement target) {
      super(start, target);
      if (!start.equals(Statement.epsilon()) && !start.getMethod().equals(target.getMethod())) {
        throw new RuntimeException("Illegal Control Flow Graph Edge constructed");
      }
    }

    @Override
    public String toString() {
      return getStart() + " -> " + getTarget();
    }

    public Statement getStart() {
      return getX();
    }

    public Statement getTarget() {
      return getY();
    }

    public Method getMethod() {
      return getStart().getMethod();
    }

    @Override
    public int hashCode() {
      return Objects.hash(toString());
    }

    // Using string-based equality and hashcode as a workaround
    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      Edge other = (Edge) obj;
      return toString().equals(other.toString());
    }

    @Override
    public boolean accepts(Location other) {
      return this.equals(other);
    }
  }
}
