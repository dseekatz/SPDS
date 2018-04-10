package boomerang.poi;

import boomerang.ForwardQuery;
import boomerang.Query;
import boomerang.jimple.Field;
import boomerang.jimple.Statement;
import boomerang.jimple.Val;
import boomerang.solver.AbstractBoomerangSolver;
import sync.pds.solver.nodes.GeneratedState;
import sync.pds.solver.nodes.INode;
import sync.pds.solver.nodes.Node;
import sync.pds.solver.nodes.SingleNode;
import wpds.impl.Transition;
import wpds.impl.Weight;
import wpds.impl.WeightedPAutomaton;
import wpds.interfaces.WPAStateListener;
import wpds.interfaces.WPAUpdateListener;

public class ExecuteImportFieldStmtPOI<W extends Weight> extends AbstractExecuteImportPOI<W> {

	private final Val baseVar;
	private final Val storedVar;
	private boolean load;
	
	public ExecuteImportFieldStmtPOI(final AbstractBoomerangSolver<W> baseSolver, AbstractBoomerangSolver<W> flowSolver,
			AbstractPOI<Statement, Val, Field> poi, Statement succ) {
		super(baseSolver, flowSolver, poi.getStmt(), succ);
		this.baseVar = poi.getBaseVar();
		this.storedVar = poi.getStoredVar();
	}
	

	public ExecuteImportFieldStmtPOI(final AbstractBoomerangSolver<W> baseSolver, AbstractBoomerangSolver<W> flowSolver,
			AbstractPOI<Statement, Val, Field> poi, Statement fieldLoadBwSucc, Statement fieldLoad) {
		super(baseSolver, flowSolver, fieldLoadBwSucc,fieldLoad);
		//curr == fieldLoadFwdSucc
		//succ = fieldLoad
		this.baseVar = poi.getBaseVar();
		this.storedVar = poi.getStoredVar();
		this.load = true;
	}

	public void execute(ForwardQuery baseAllocation, Query flowAllocation) {

	}

	public void solve() {
		baseSolver.getFieldAutomaton()
				.registerListener(new WPAUpdateListener<Field, INode<Node<Statement, Val>>, W>() {

					@Override
					public void onWeightAdded(Transition<Field, INode<Node<Statement, Val>>> t, W w,
							WeightedPAutomaton<Field, INode<Node<Statement, Val>>, W> aut) {
						final INode<Node<Statement, Val>> aliasedVariableAtStmt = t.getStart();
						
						if (!t.getStart().fact().stmt().equals(succ))
							return;
						if (!(aliasedVariableAtStmt instanceof GeneratedState)) {
							Val alias = aliasedVariableAtStmt.fact().fact();
							if (alias.equals(baseVar) && t.getLabel().equals(Field.empty())) {
								// t.getTarget is the allocation site
								WeightedPAutomaton<Field, INode<Node<Statement, Val>>, W> baseAutomaton = baseSolver
										.getFieldAutomaton();
								if(!load) {
									baseAutomaton.registerListener(
											new ImportBackwards(t.getTarget(), new DirectCallback(t.getStart())));
								} else {
									Node<Statement, Val> aliasedVarAtSucc = new Node<Statement, Val>(curr, alias);
									baseAutomaton.registerListener(
											new ImportBackwards(t.getTarget(), new DirectCallback(new SingleNode<Node<Statement, Val>>(aliasedVarAtSucc))));
								}
							}
						}
						
					}
				});
	}


	@Override
	protected WPAStateListener<Field, INode<Node<Statement, Val>>, W> createImportBackwards(
			INode<Node<Statement, Val>> target, Callback callback) {
		return new ImportBackwards(target,callback);
	}

	private class ImportBackwards extends WPAStateListener<Field, INode<Node<Statement, Val>>, W> {

		private Callback callback;

		public ImportBackwards(INode<Node<Statement, Val>> iNode, Callback callback) {
			super(iNode);
			this.callback = callback;
		}

		@Override
		public void onOutTransitionAdded(Transition<Field, INode<Node<Statement, Val>>> t, W w,
				WeightedPAutomaton<Field, INode<Node<Statement, Val>>, W> weightedPAutomaton) {
		}

		@Override
		public void onInTransitionAdded(Transition<Field, INode<Node<Statement, Val>>> t, W w,
				WeightedPAutomaton<Field, INode<Node<Statement, Val>>, W> weightedPAutomaton) {
			if(t.getLabel().equals(Field.epsilon()))
				return;
			
			if (!(t.getStart() instanceof GeneratedState) && t.getStart().fact().stmt().equals((!load ? curr : succ))
					&& !t.getStart().fact().fact().equals(baseVar)) {
				Val alias = t.getStart().fact().fact();
				Node<Statement, Val> aliasedVarAtSucc = new Node<Statement, Val>(succ, alias);
				Node<Statement, Val> rightOpNode = new Node<Statement, Val>((!load ? curr : succ), storedVar);
				callback.trigger(new Transition<Field, INode<Node<Statement, Val>>>(
						new SingleNode<Node<Statement, Val>>(aliasedVarAtSucc), t.getLabel(), t.getTarget()));
				flowSolver.setFieldContextReachable(aliasedVarAtSucc);
				flowSolver.addNormalCallFlow(rightOpNode, aliasedVarAtSucc);
			}
			if (t.getStart() instanceof GeneratedState) {
				baseSolver.getFieldAutomaton()
						.registerListener(new ImportBackwards(t.getStart(), new TransitiveCallback(callback, t)));
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + getOuterType().hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			ImportBackwards other = (ImportBackwards) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			return true;
		}

		private ExecuteImportFieldStmtPOI getOuterType() {
			return ExecuteImportFieldStmtPOI.this;
		}

	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((baseVar == null) ? 0 : baseVar.hashCode());
		result = prime * result + ((storedVar == null) ? 0 : storedVar.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExecuteImportFieldStmtPOI other = (ExecuteImportFieldStmtPOI) obj;
		if (baseVar == null) {
			if (other.baseVar != null)
				return false;
		} else if (!baseVar.equals(other.baseVar))
			return false;
		if (storedVar == null) {
			if (other.storedVar != null)
				return false;
		} else if (!storedVar.equals(other.storedVar))
			return false;
		return true;
	}

}