import { Injectable } from "chevronjs";
import { chevron } from "../chevron";
import { ProposedArtistEdit } from "./artist/enricher/ArtistEnricherService";
import { EditType } from "./ProposedEdit";

@Injectable(chevron)
class ProposedEditService {
    public stringifyProposedArtistEdit(
        proposedEdit: ProposedArtistEdit
    ): string {
        const prefix = `${proposedEdit.type}: '${proposedEdit.target.name}' ${proposedEdit.property}`;
        return proposedEdit.type === EditType.CHANGE
            ? `${prefix} ${JSON.stringify(
                  proposedEdit.old
              )} -> ${JSON.stringify(proposedEdit.new)}`
            : `${prefix} ${JSON.stringify(proposedEdit.new)}`;
    }
}

export { ProposedEditService };
