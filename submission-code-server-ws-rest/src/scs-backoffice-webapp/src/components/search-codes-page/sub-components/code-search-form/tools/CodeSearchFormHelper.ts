import {Dispatch, SetStateAction} from "react";

/**
 * Helper method of react component CodeSearchForm
 * It parses actual path to set default value of InitialLot and InitialElementPerPage at page loading
 *
 * @param actualPath path from useLocation().path - it can only be given in a react component.
 * @param basePath - path set in props. it represent the base path of redirection to the CodeSearchForm
 * @param setInitialLot - setState of InitialLot given by useState
 * @param setInitialElementPerPage - setState of InitialElementPerPage given by useState
 */
export function parseBasePath(
    actualPath:string,
    basePath : string,
    setInitialLot : Dispatch<SetStateAction<string>>,
    setInitialElementPerPage:Dispatch<SetStateAction<number>>
) {
    if (actualPath.startsWith(basePath)) {

        const splittedPath = actualPath
            .replace(basePath, "")
            .split("/");

        if(splittedPath[1]) setInitialLot(splittedPath[1])
        if(splittedPath[2]) setInitialElementPerPage(parseInt(splittedPath[2]))
    }
}