package org.igov.service.business.subject.criteria;

import static org.igov.service.business.subject.SubjectGroupTreeService.HUMAN;
import static org.igov.service.business.subject.SubjectGroupTreeService.ORGAN;

public class HierarchyCriteria {

	public static final int BOTTOM = -1;
	public static final int TOP = 1;

	public static final int TREE = -1;
	public static final int LAST = 0;

	private String sRoot;
	private String sType = ORGAN;
	private int nLevel = TREE;
	private int nDirection = BOTTOM;
	private boolean bIncludeRoot = false;

	private int curLevel = 0;

	private String sDirection;

	public HierarchyCriteria() {

	}

	public static HierarchyCriteria create(String sRoot) {
		return new HierarchyCriteria().setsRoot(sRoot);
	}

	public static HierarchyCriteria bossOf(String sRoot) {
		return new HierarchyCriteria().setsRoot(sRoot).setsType(HUMAN).setnDirection(TOP).setnLevel(1);
	}

	public static HierarchyCriteria companyOf(String sRoot) {
		return new HierarchyCriteria().setsRoot(sRoot).setsType(ORGAN).setnDirection(TOP).setnLevel(LAST);
	}

	public static HierarchyCriteria parentDepartOf(String sRoot) {
		return new HierarchyCriteria().setsRoot(sRoot).setsType(ORGAN).setnDirection(TOP).setnLevel(1);
	}

	public static HierarchyCriteria departsUp(String sRoot) {
		return new HierarchyCriteria().setsRoot(sRoot).setsType(ORGAN).setnDirection(TOP).setnLevel(TREE);
	}

	public static HierarchyCriteria employeesOf(String sRoot) {
		return new HierarchyCriteria().setsRoot(sRoot).setsType(HUMAN).setnDirection(BOTTOM).setnLevel(1);
	}

	public static HierarchyCriteria bottomDeparts(String sRoot) {
		return new HierarchyCriteria().setsRoot(sRoot).setsType(ORGAN).setnDirection(BOTTOM).setnLevel(1);
	}

	public HierarchyCriteria setsRoot(String sRoot) {
		this.sRoot = sRoot;
		return this;
	}

	public HierarchyCriteria setsType(String sType) {
		this.sType = sType;
		return this;
	}

	public HierarchyCriteria setnLevel(int nLevel) {
		this.nLevel = nLevel;
		return this;
	}

	public HierarchyCriteria setnDirection(int nDirection) {
		this.nDirection = nDirection;
		this.sDirection = nDirection < 0 ? "oSubjectGroup_Parent" : "oSubjectGroup_Child";
		return this;
	}

	public HierarchyCriteria setbIncludeRoot(boolean bIncludeRoot) {
		this.bIncludeRoot = bIncludeRoot;
		return this;
	}

	public String getsRoot() {
		return sRoot;
	}

	public String getsType() {
		return sType;
	}

	public int getnLevel() {
		return nLevel;
	}

	public int getnDirection() {
		return nDirection;
	}

	public boolean isbIncludeRoot() {
		return bIncludeRoot;
	}

	public String getsDirection() {
		return sDirection;
	}

	public int getCurLevel() {
		return curLevel;
	}

	public void setCurLevel(int curLevel) {
		this.curLevel = curLevel;
	}

	public void up() {
		this.curLevel++;
	}

	public void down() {
		this.curLevel--;
	}

	@Override
	public String toString() {
		String direction = nDirection == BOTTOM ? "BOTTOM" : "TOP";
		return "root: " + sRoot
				+ ", type: " + sType
				+ ", direction: " + direction
				+ ", depth: " + nLevel;
	}

	public static enum Direction {
		BOTTOM,
		TOP
	}

}
