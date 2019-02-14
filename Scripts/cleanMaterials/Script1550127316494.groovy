import com.kazurayam.materials.Helpers
import com.kazurayam.materials.MaterialRepository

import internal.GlobalVariable as GlobalVariable

/**
 * delete the contents of the ./Materials directory
 */
MaterialRepository mr = (MaterialRepository)GlobalVariable.MATERIAL_REPOSITORY
assert mr != null

Helpers.deleteDirectoryContents(mr.getBaseDir())