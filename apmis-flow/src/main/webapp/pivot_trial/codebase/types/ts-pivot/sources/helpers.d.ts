import { DateFormatter } from "./DateFormatter";
import { IField } from "./types";
export declare const dateFormatter: DateFormatter;
export declare const groupMethods: {
    dateByDay: (val: any) => {
        id: string;
        label: string;
    };
    dateByMonth: (val: any) => {
        id: string;
        label: string;
    };
    dateByQuarter: (val: any) => {
        id: string;
        label: string;
    };
    dateByWeek: (val: any) => {
        id: string;
        label: string;
    };
    dateByYear: (val: any) => {
        id: string;
        label: string;
    };
};
export declare function strToDate(val: any, format?: string): Date;
export declare function dateToStr(val: any, format: string): string;
export declare function getUniqueFieldData(field: IField, data: any[]): any[];
