package org.luaj.kahluafork.compiler;

/**
 * @exclude
 */
public class ExpDesc {
	int k; // expkind, from enumerated list, above

	int info, aux;
	private double _nval;
	private boolean has_nval;
	public void setNval(double r) {
		_nval = r;
		has_nval = true;
	}
	public double nval() {
		return has_nval ? _nval : info;
	}

	int t; /* patch list of `exit when true' */
	int f; /* patch list of `exit when false' */
	void init( int k, int i ) {
		this.f = LexState.NO_JUMP;
		this.t = LexState.NO_JUMP;
		this.k = k;
		this.info = i;
	}

	boolean hasjumps() {
		return (t != f);
	}

	boolean isnumeral() {
		return (k == LexState.VKNUM && t == LexState.NO_JUMP && f == LexState.NO_JUMP);
	}

	public void setvalue(ExpDesc other) {
		this.k = other.k;
		this._nval = other._nval;
		this.has_nval = other.has_nval;
		this.info = other.info;
		this.aux = other.aux;
		this.t = other.t;
		this.f = other.f;
	}
}
