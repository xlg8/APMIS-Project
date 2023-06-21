import { IEventSystem } from "../../ts-common/events";
import { ITreeGrid } from "../../ts-treegrid";
import { ILayout } from "../../ts-layout";
export interface IPivot {
    grid: ITreeGrid;
    layout: ILayout;
    export: any;
    destructor(): void;
    setFields(fields: IPivotFields): void;
    getFields(): IPivotFields;
    getConfig(): IPivotConfig;
    load(url: string, type?: string): Promise<any>;
    setData(data: any[]): void;
    addMathMethod(id: string, label: string, func: mathMethod): void;
    addSubField(name: string, functor: any, label: string): void;
    setFilterValue(fieldId: string, meta: IFilterMeta): void;
    setFiltersValues(filters: {
        [key: string]: IFilterMeta;
    }): void;
    getFilterValue(fieldId: string): IFilterMeta;
    getFiltersValues(): IFilterMeta[];
    clearFiltersValues(): any;
    setGlobalFilter(handler?: any): void;
    paint(): void;
}
export interface IPivotConfig {
    data: any[];
    fields: IRawPivotFields;
    fieldList: IField[];
    types?: ITypes;
    layout?: IPivotLayout;
    mark?: IMark | MarkFunction;
    dataFormat?: any;
    customFormat?: any;
}
export interface IPivotLayout {
    leftMargin?: number;
    gridOnly?: boolean;
    readonly?: boolean;
    liveReload?: boolean;
    columnsWidth?: number | "auto";
    fieldsCloseBtn?: boolean;
    showFilters?: boolean;
    footer?: boolean;
    fieldSelectorType?: "loop" | "dropdown";
    repeatRowsHeaders?: boolean;
    gridMode?: GridMode;
}
export declare type GridMode = "tree" | "flat";
export declare type Template = (text: string, field: IField) => string;
export declare type CellTemplate = (text: string, row: any, col: any) => string;
export interface IField {
    id: string;
    label?: string;
    type?: string;
    group?: string;
    method?: string;
    format?: string;
    inputFormat?: string;
    sortDir?: string;
    template?: Template;
    cellTemplate?: CellTemplate;
    aliases?: {
        [key: string]: {
            [key: string]: string | number;
        };
    };
    $filter?: boolean;
    $uid?: string;
    $field?: string;
}
export interface IValue extends IField {
    id: string;
    method: string;
    label?: string;
}
export interface ITypes {
    operations?: Array<{
        id: string;
        label: string;
    }>;
    dates?: Array<{
        id: string;
        label: string;
    }>;
}
export interface IRawPivotFields {
    columns: Array<IField | string>;
    values: IValue[];
    rows: Array<IField | string>;
}
export interface IPivotFields extends IRawPivotFields {
    columns: IField[];
    rows: IField[];
    values: IValue[];
    free?: IField[];
}
declare type MarkFunction = (cell: any, columnCells: any[], row: any, column: any) => string;
export interface IMark {
    min?: string;
    max?: string;
}
export interface IParsedPivotConfig extends IPivotConfig {
    fields: IPivotFields;
}
export interface IConfiguratorConfig extends IParsedPivotConfig {
    events: IEventSystem<PivotEvents>;
}
export declare type mathMethod = (cellData: number[]) => number | null;
interface IPivotLocalization {
    availableFields: string;
    data: string;
    columns: string;
    rows: string;
    moveFieldsHere: string;
    hideSettings: string;
    showSettings: string;
    apply: string;
    day: string;
    month: string;
    quarter: string;
    year: string;
    min: string;
    max: string;
    sum: string;
    count: string;
    date: any;
}
export interface IDHXGlobal {
    i18n: {
        pivot: IPivotLocalization;
    };
}
export interface IDataPreprocessor {
    getData(data: any[], fields: IPivotFields): any[];
}
export interface IExportConfig {
    name?: string;
    url?: string;
}
export interface IPivotRichData {
    aliases?: {
        [key: string]: {
            [key: string]: string | number;
        };
    };
    data?: any[];
}
export declare type IPivotData = IPivotRichData | string[] | any;
export interface IPopup {
    attachEvent(name: string, callback: any): any;
    detachEvent(name: string): any;
    callEvent(name: string, args: any[]): any;
    addDomEvent(name: string): any;
    removeDomEvent(name: string): any;
    show(node: HTMLElement): any;
    hide(): any;
}
export declare type IFilterType = "string" | "number" | "date";
export interface IFilterMeta {
    operation?: string;
    filter?: string | {
        from: string;
        to: string;
    };
    values?: string[];
}
export interface IDateFilterMeta extends IFilterMeta {
    filter?: {
        from: string;
        to: string;
    };
    type: string;
    inputFormat?: string;
}
export interface IFilterPopup extends IPopup {
    getFilter(): IFilterMeta;
    setFilter(meta: IFilterMeta): any;
    setOptions(data: any[]): any;
    isActive(): boolean;
}
export interface IFilterInfo {
    handler: (value: any) => boolean;
    meta?: IFilterMeta;
}
export interface IPos {
    top: number;
    left: number;
}
export declare enum PivotEvents {
    fieldClick = "fieldClick",
    applyButtonClick = "applyButtonClick",
    change = "change",
    update = "update",
    filterApply = "filterApply",
    filterCancel = "filterCancel"
}
export interface ICell {
    $keyArr: string[];
    id?: any;
}
export interface IShadow {
    paint(): void;
    destructor(): void;
}
export {};
