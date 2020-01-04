"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", { value: true });
const chevronjs_1 = require("chevronjs");
const chevron_1 = require("../chevron");
const ProposedEdit_1 = require("./ProposedEdit");
let ProposedEditService = class ProposedEditService {
    stringifyProposedArtistEdit(proposedEdit) {
        const prefix = `${proposedEdit.type}: '${proposedEdit.target.name}' ${proposedEdit.property}`;
        return proposedEdit.type === ProposedEdit_1.EditType.CHANGE
            ? `${prefix} ${JSON.stringify(proposedEdit.old)} -> ${JSON.stringify(proposedEdit.new)}`
            : `${prefix} ${JSON.stringify(proposedEdit.new)}`;
    }
};
ProposedEditService = __decorate([
    chevronjs_1.Injectable(chevron_1.chevron)
], ProposedEditService);
exports.ProposedEditService = ProposedEditService;
