package boomerang.results;

import java.util.Set;

import boomerang.BackwardQuery;
import boomerang.ForwardQuery;
import boomerang.jimple.Field;
import boomerang.jimple.Statement;
import boomerang.jimple.Val;
import sync.pds.solver.nodes.INode;
import sync.pds.solver.nodes.Node;
import wpds.impl.Transition;
import wpds.impl.Weight;
import wpds.impl.WeightedPAutomaton;
import wpds.interfaces.WPAStateListener;

public abstract class ExtractAllocationSiteStateListener<W extends Weight>
        extends WPAStateListener<Field, INode<Node<Statement, Val>>, W> {

    /**
     * 
     */
    private ForwardQuery query;
    private BackwardQuery bwQuery;

    public ExtractAllocationSiteStateListener(INode<Node<Statement, Val>> state, BackwardQuery bwQuery,
            ForwardQuery query) {
        super(state);
        this.bwQuery = bwQuery;
        this.query = query;
    }

    @Override
    public void onOutTransitionAdded(Transition<Field, INode<Node<Statement, Val>>> t, W w,
            WeightedPAutomaton<Field, INode<Node<Statement, Val>>, W> weightedPAutomaton) {
    }

    @Override
    public void onInTransitionAdded(Transition<Field, INode<Node<Statement, Val>>> t, W w,
            WeightedPAutomaton<Field, INode<Node<Statement, Val>>, W> weightedPAutomaton) {
        if (t.getStart().fact().equals(bwQuery.asNode()) && t.getLabel().equals(Field.empty())) {
            allocationSiteFound(query,bwQuery);
        }
    }

    protected abstract void allocationSiteFound(ForwardQuery allocationSite, BackwardQuery query);

    @Override
    public int hashCode() {
        // Otherwise we cannot register this listener twice.
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        // Otherwise we cannot register this listener twice.
        return this == obj;
    }
}