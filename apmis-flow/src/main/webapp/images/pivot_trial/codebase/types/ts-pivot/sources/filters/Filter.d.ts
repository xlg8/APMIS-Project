import { View } from "../../../ts-common/view";
import { Popup } from "../../../ts-popup";
import { IField, IFilterMeta } from "../types";
export declare const operations: {
    string: {
        ct: {
            name: () => string;
            handler: (item: string, str: string) => boolean;
        };
        nct: {
            name: () => string;
            handler: (item: string, str: string) => boolean;
        };
        eq: {
            name: () => string;
            handler: (item: string, str: string) => boolean;
        };
        neq: {
            name: () => string;
            handler: (item: string, str: string) => boolean;
        };
    };
    number: {
        lt: {
            name: () => string;
            handler: (item: number, inpVal: number) => boolean;
        };
        gt: {
            name: () => string;
            handler: (item: number, inpVal: number) => boolean;
        };
        leq: {
            name: () => string;
            handler: (item: number, inpVal: number) => boolean;
        };
        geq: {
            name: () => string;
            handler: (item: number, inpVal: number) => boolean;
        };
        eq: {
            name: () => string;
            handler: (item: number, inpVal: number) => boolean;
        };
        neq: {
            name: () => string;
            handler: (item: number, inpVal: number) => boolean;
        };
    };
};
export declare class Filter extends View {
    config: any;
    protected _meta: IFilterMeta;
    protected _htmlEvents: any;
    protected _inputTimeout: any;
    protected _data: any[];
    protected _item: IField;
    protected _popup: Popup;
    constructor(config: any);
    setMeta(item: IField, meta: IFilterMeta, uniqueData: any[]): void;
    getMeta(): IFilterMeta;
    show(node: HTMLElement): void;
    hide(): void;
    isVisible(): boolean;
    getItem(): IField;
    protected _setMeta(oldMeta: IFilterMeta): void;
    protected _checkHandler(checkbox: any): void;
    protected _validateInput(val: any): boolean;
    protected _getContent(): any;
    protected _getOperations(type?: string): any[];
    protected _getFilterInput(): any;
    protected _formatLabel(label: any): any;
    protected _getCheckboxData(): any[];
    protected _getCheckboxes(): any;
}
