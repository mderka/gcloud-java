/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gcloud.resourcemanager;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Google Cloud Resource Manager project object.
 *
 * <p>A Project is a high-level Google Cloud Platform entity. It is a container for ACLs, APIs,
 * AppEngine Apps, VMs, and other Google Cloud Platform resources. This class' member variables are
 * immutable.  Methods that change or update the underlying Project information return a new Project
 * instance.
 */
public class Project {

  private final ResourceManager resourceManager;
  private final ProjectInfo info;

  /**
   * Constructs a Project object that contains the ProjectInfo given.
   */
  public Project(ResourceManager resourceManager, ProjectInfo projectInfo) {
    this.resourceManager = checkNotNull(resourceManager);
    this.info = checkNotNull(projectInfo);
  }

  /**
   * Constructs a Project object that contains project information got from the server.
   *
   * @return Project object containing the project's metadata or {@code null} if not found
   * @throws ResourceManagerException upon failure
   */
  public static Project get(ResourceManager resourceManager, String projectId) {
    ProjectInfo projectInfo = resourceManager.get(projectId);
    return projectInfo != null ? new Project(resourceManager, projectInfo) : null;
  }

  /**
   * Returns the {@link ProjectInfo} object associated with this Project.
   */
  public ProjectInfo info() {
    return info;
  }

  /**
   * Returns the {@link ResourceManager} service object associated with this Project.
   */
  public ResourceManager resourceManager() {
    return resourceManager;
  }

  /**
   * Fetches the current project's latest information. Returns {@code null} if the job does not
   * exist.
   *
   * @return Project containing the project's updated metadata or {@code null} if not found
   * @throws ResourceManagerException upon failure
   */
  public Project reload() {
    return Project.get(resourceManager, info.projectId());
  }

  /**
   * Marks the project identified by the specified project ID for deletion.
   *
   * <p>This method will only affect the project if the following criteria are met:
   * <ul>
   * <li>The project does not have a billing account associated with it.
   * <li>The project has a lifecycle state of {@link ProjectInfo.State#ACTIVE}.
   * </ul>
   * This method changes the project's lifecycle state from {@link ProjectInfo.State#ACTIVE} to
   * {@link ProjectInfo.State#DELETE_REQUESTED}. The deletion starts at an unspecified time, at
   * which point the lifecycle state changes to {@link ProjectInfo.State#DELETE_IN_PROGRESS}. Until
   * the deletion completes, you can check the lifecycle state checked by retrieving the project
   * with {@link ResourceManager#get}, and the project remains visible to
   * {@link ResourceManager#list}. However, you cannot update the project. After the deletion
   * completes, the project is not retrievable by the {@link ResourceManager#get} and
   * {@link ResourceManager#list} methods. The caller must have modify permissions for this project.
   *
   * @see <a
   * href="https://cloud.google.com/resource-manager/reference/rest/v1beta1/projects/delete">Cloud
   * Resource Manager delete</a>
   * @throws ResourceManagerException upon failure
   */
  public void delete() {
    resourceManager.delete(info.projectId());
  }

  /**
   * Restores the project identified by the specified project ID.
   *
   * <p>You can only use this method for a project that has a lifecycle state of
   * {@link ProjectInfo.State#DELETE_REQUESTED}. After deletion starts, as indicated by a lifecycle
   * state of {@link ProjectInfo.State#DELETE_IN_PROGRESS}, the project cannot be restored. The
   * caller must have modify permissions for this project.
   *
   * @see <a
   * href="https://cloud.google.com/resource-manager/reference/rest/v1beta1/projects/undelete">Cloud
   * Resource Manager undelete</a>
   * @throws ResourceManagerException upon failure (including when the project can't be restored)
   */
  public void undelete() {
    resourceManager.undelete(info.projectId());
  }

  /**
   * Replaces the attributes of the project.
   *
   * <p>The caller must have modify permissions for this project.
   *
   * @see <a
   * href="https://cloud.google.com/resource-manager/reference/rest/v1beta1/projects/update">Cloud
   * Resource Manager update</a>
   * @return the ProjectInfo representing the new project metadata
   * @throws ResourceManagerException upon failure
   */
  public Project replace(ProjectInfo projectInfo) {
    return new Project(resourceManager, resourceManager.replace(checkNotNull(projectInfo)));
  }
}
