import { IDateFilterMeta } from "../types";
import { Filter } from "./Filter";
export declare class DateFilter extends Filter {
    protected _format: any;
    protected mask: any;
    protected placeholder: any;
    protected _meta: IDateFilterMeta;
    protected _inputTimeout: any;
    constructor(config: any);
    protected _checkInputText(e: any, inputId: string): void;
    protected _validateInput(val: any): any;
    protected _createMask(): void;
    protected _setMeta(oldMeta: IDateFilterMeta): void;
    protected _getFilterInput(): any;
    protected _formatLabel(label: any): any;
    protected _getCheckboxData(): any[];
}
