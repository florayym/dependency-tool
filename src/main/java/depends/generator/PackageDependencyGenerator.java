package depends.generator;

import depends.entity.CandidateTypes;
import depends.entity.Entity;
import depends.entity.PackageEntity;
import depends.entity.repo.EntityRepo;
import depends.matrix.core.DependencyMatrix;
import depends.relations.Relation;

import java.util.Iterator;
import java.util.List;

public class PackageDependencyGenerator extends DependencyGenerator {
    @Override
    public DependencyMatrix build(EntityRepo entityRepo, List<String> typeFilter) {
        DependencyMatrix dependencyMatrix = new DependencyMatrix(typeFilter);
        Iterator<Entity> iterator = entityRepo.entityIterator();
        System.out.println("Start creating dependency matrix....");
        while(iterator.hasNext()) {
            Entity entity = iterator.next();
            if (!entity.inScope()) { // true
                continue;
            }
            if (entity instanceof PackageEntity) {
                String name = stripper.stripFilename(entity.getDisplayName());
                name = filenameWritter.reWrite(name);
                dependencyMatrix.addNode(name, entity.getId());
            }
            int packageEntityFrom = getPackageEntityIdNoException(entityRepo, entity);
            if (packageEntityFrom == -1) {
                continue;
            }
            for (Relation relation : entity.getRelations()) {
                Entity relatedEntity = relation.getEntity();
                if (relatedEntity == null) {
                    continue;
                }
                if (relatedEntity instanceof CandidateTypes) {
                    /* TODO */
                } else {
                    if (relatedEntity.getId() >= 0) {
                        int packageEntityTo = getPackageEntityIdNoException(entityRepo, relatedEntity);
                        if (packageEntityTo != -1) {
                            dependencyMatrix.addDependency(relation.getType(), packageEntityFrom, packageEntityTo, 1, buildDescription(entity, relatedEntity, relation.getFromLine()));
                        }
                    }
                }
            }
        }
        System.out.println("Finish creating dependency matrix....");

        return dependencyMatrix;
    }

    private int getPackageEntityIdNoException(EntityRepo entityRepo, Entity entity) {
        Entity ancestor = entity.getAncestorOfType(PackageEntity.class);
        if (ancestor == null) {
            return -1;
        }
        if (!ancestor.inScope()) {
            return -1;
        }
        return ancestor.getId();
    }
}
