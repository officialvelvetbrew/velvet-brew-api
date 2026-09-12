package com.cafe.velvetbrew.dto;

import lombok.Builder;
import lombok.Data;

/**
 * A user's effective create/read/update/delete capability on one RBAC
 * function, merged across every role they hold (if two roles disagree, the
 * more permissive flag wins). Meant for a front end to key screen/button
 * visibility on directly, e.g. hide the delete button unless
 * functions["INVENTORY_ITEMS"].canDelete is true.
 */
@Data
@Builder
public class FunctionPermissionResponse {

    private String functionCode;

    private boolean canCreate;

    private boolean canRead;

    private boolean canUpdate;

    private boolean canDelete;
}
