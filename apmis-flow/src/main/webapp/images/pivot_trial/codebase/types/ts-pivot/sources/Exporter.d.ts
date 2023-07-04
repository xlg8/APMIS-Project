import { IPivot } from "./types";
import { ICsvExportConfig, IXlsxExportConfig } from "../../ts-grid";
export declare function getExporter(pivot: IPivot): {
    (config: IXlsxExportConfig): {
        name: string;
        columns: any[];
        header: any[][];
        data: string[][];
        styles: {
            cells: any[];
            css: {
                default: {
                    color: string;
                    background: string;
                    fontSize: number;
                };
            };
        };
    };
    csv(config: ICsvExportConfig): string;
    xlsx(config: IXlsxExportConfig): {
        name: string;
        columns: any[];
        header: any[][];
        data: string[][];
        styles: {
            cells: any[];
            css: {
                default: {
                    color: string;
                    background: string;
                    fontSize: number;
                };
            };
        };
    };
};
