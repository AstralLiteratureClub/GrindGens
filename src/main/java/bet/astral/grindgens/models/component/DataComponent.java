/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.models.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public interface DataComponent extends Component {
	JsonElement dataAsJson() throws JsonParseException;
}