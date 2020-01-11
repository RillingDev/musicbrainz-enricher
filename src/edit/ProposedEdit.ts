const enum EditType {
    ADD = "ADD",
    CHANGE = "CHECK/CHANGE"
}

interface ProposedEdit<TTarget, UValue> {
    type: EditType;
    target: TTarget;
    property: string;
    old?: UValue;
    new: UValue;
}

export { ProposedEdit, EditType };
