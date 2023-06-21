import { IDataPreprocessor, IField, IFilterMeta, IParsedPivotConfig, IPivotConfig, IPivotData, IPivotFields } from "./types";
export declare class DataPreprocessor implements IDataPreprocessor {
    private _filters;
    private _subFields;
    private _globalFilter;
    private _fields;
    constructor();
    setFilterValue(fieldId: string, meta: IFilterMeta): void;
    setFiltersValues(filtersMeta: any): void;
    getFilterValue(fieldId: string): IFilterMeta;
    getFiltersValues(): any;
    clearFiltersValues(): void;
    setGlobalFilter(handler?: any): void;
    addSubField(name: string, functor: any, label: string): void;
    normalizeConfig(config: IPivotConfig): IParsedPivotConfig;
    normalizeData(data: IPivotData, fieldList: any): any[];
    normalizeFields(config: any): void;
    getData(data: any[], fields: IPivotFields): any[];
    getPureFields(fields: IPivotFields): {
        columns: IField[];
        rows: IField[];
        values: import("./types").IValue[];
    };
    private _parseDates;
    private _parseAliases;
    private _checkAliases;
    private _checkData;
    private _callGroupMethod;
}
