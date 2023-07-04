import { IEventSystem } from "../../../ts-common/events";
import { IField, IFilterMeta, PivotEvents } from "../types";
import { DateFilter } from "./DateFilter";
import { Filter } from "./Filter";
export declare function getFilterHandler(meta: IFilterMeta, field: IField): (item: any) => boolean;
export declare class Filters {
    events: IEventSystem<PivotEvents>;
    private _filter;
    constructor(events: IEventSystem<PivotEvents>);
    getFilter(item: IField, coords: any, data: any[], oldMeta: IFilterMeta): void;
    protected _getFilter(type: string): Filter | DateFilter;
    protected _getFieldType(fieldData: any[]): "string" | "number" | "bigint" | "boolean" | "symbol" | "undefined" | "object" | "function";
}
