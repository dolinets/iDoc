package org.igov.model.action.vo;

/**
 *
 * @author idenysenko
 */
public class TaskCountersVO {
    
    private int Closed;
    private int OpenedUnassigned;
    private int Control;
    private int OpenedAssigned;
    private int DocumentOpenedUnassignedProcessed;
    private int DocumentOpenedUnassignedUnprocessed;
    private int DocumentOpenedUnassignedWithoutECP;
    private int DocumentClosed;
    private int OpenedCreatorDocument;
    private long Ticket;
    private int Execution;

    public TaskCountersVO() {
    }

    public int getClosed() {
        return Closed;
    }

    public void setClosed(int Closed) {
        this.Closed = Closed;
    }

    public int getOpenedUnassigned() {
        return OpenedUnassigned;
    }

    public void setOpenedUnassigned(int OpenedUnassigned) {
        this.OpenedUnassigned = OpenedUnassigned;
    }

    public int getControl() {
        return Control;
    }

    public void setControl(int Control) {
        this.Control = Control;
    }

    public int getOpenedAssigned() {
        return OpenedAssigned;
    }

    public void setOpenedAssigned(int OpenedAssigned) {
        this.OpenedAssigned = OpenedAssigned;
    }

    public int getDocumentOpenedUnassignedProcessed() {
        return DocumentOpenedUnassignedProcessed;
    }

    public void setDocumentOpenedUnassignedProcessed(int DocumentOpenedUnassignedProcessed) {
        this.DocumentOpenedUnassignedProcessed = DocumentOpenedUnassignedProcessed;
    }

    public int getDocumentOpenedUnassignedUnprocessed() {
        return DocumentOpenedUnassignedUnprocessed;
    }

    public void setDocumentOpenedUnassignedUnprocessed(int DocumentOpenedUnassignedUnprocessed) {
        this.DocumentOpenedUnassignedUnprocessed = DocumentOpenedUnassignedUnprocessed;
    }

    public int getDocumentOpenedUnassignedWithoutECP() {
        return DocumentOpenedUnassignedWithoutECP;
    }

    public void setDocumentOpenedUnassignedWithoutECP(int DocumentOpenedUnassignedWithoutECP) {
        this.DocumentOpenedUnassignedWithoutECP = DocumentOpenedUnassignedWithoutECP;
    }

    public int getDocumentClosed() {
        return DocumentClosed;
    }

    public void setDocumentClosed(int DocumentClosed) {
        this.DocumentClosed = DocumentClosed;
    }

    public int getOpenedCreatorDocument() {
        return OpenedCreatorDocument;
    }

    public void setOpenedCreatorDocument(int OpenedCreatorDocument) {
        this.OpenedCreatorDocument = OpenedCreatorDocument;
    }

    public long getTicket() {
        return Ticket;
    }

    public void setTicket(long Ticket) {
        this.Ticket = Ticket;
    }

    public int getExecution() {
        return Execution;
    }

    public void setExecution(int Execution) {
        this.Execution = Execution;
    }

    @Override
    public String toString() {
        return "TaskCountersVO{"
                + "Closed=" + Closed
                + ", OpenedUnassigned=" + OpenedUnassigned
                + ", Control=" + Control
                + ", OpenedAssigned=" + OpenedAssigned
                + ", DocumentOpenedUnassignedProcessed=" + DocumentOpenedUnassignedProcessed
                + ", DocumentOpenedUnassignedUnprocessed=" + DocumentOpenedUnassignedUnprocessed
                + ", DocumentOpenedUnassignedWithoutECP=" + DocumentOpenedUnassignedWithoutECP
                + ", DocumentClosed=" + DocumentClosed
                + ", OpenedCreatorDocument=" + OpenedCreatorDocument
                + ", Ticket=" + Ticket
                + ", Execution=" + Execution
                + '}';
    }
    
}
