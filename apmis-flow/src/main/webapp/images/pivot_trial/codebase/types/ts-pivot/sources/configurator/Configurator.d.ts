import { IEventSystem } from "../../../ts-common/events";
import { IConfiguratorConfig, IPivotFields, PivotEvents } from "../types";
export declare const types: {
    operations: {
        id: string;
        label: string;
    }[];
    dates: {
        id: string;
        label: string;
    }[];
};
export declare class Configurator {
    config: IConfiguratorConfig;
    events: IEventSystem<PivotEvents>;
    private _pivot;
    private _pressedItem;
    private _pressedCoords;
    private _isDragStart;
    private _dropArea;
    private _changeItem;
    private _htmlEvents;
    private _filters;
    constructor(pivot: any, config: IConfiguratorConfig);
    getField(name: string): any;
    setFields(fields: IPivotFields): void;
    getFields(): IPivotFields;
    getShadow(): any;
    setPressedItem(ev: any, id: string, node: HTMLElement): void;
    private _getDropArea;
    private _getFieldObj;
    private _handleMouseMove;
    private _handleMouseup;
    private _getItem;
    private _moveItem;
    private _deleteItem;
    private _updateFields;
    private _getSelectorType;
    private _getSelector;
    private _getLoopSelect;
    private _getDropdownSelect;
    private _toggleSelector;
}
