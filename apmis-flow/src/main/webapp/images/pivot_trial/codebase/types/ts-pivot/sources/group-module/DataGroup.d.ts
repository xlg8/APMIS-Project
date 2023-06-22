import { IParsedPivotConfig } from "../types";
import { IGrid } from "../../../ts-grid";
export declare function getValue(obj: any, key: string, fallback?: any): any;
export declare function getGridData(config: IParsedPivotConfig, data: any[], grid: IGrid): {
    headers: any[];
    groupedData: any[];
};
