import { ICell } from "../types";
export declare function getUnique(data: ICell[][], field: string, dir: string): any[];
export declare const mathMethods: {
    sum: (data: any[]) => any;
    min: (data: any[]) => any;
    max: (data: any[]) => any;
    count: (data: any[]) => number;
};
export declare function methods(data: any[], field: string, method: string): any;
