/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ambari.server.orm.entities;

import javax.persistence.*;

@IdClass(org.apache.ambari.server.orm.entities.RoleSuccessCriteriaEntityPK.class)
@Table(name = "role_success_criteria", schema = "ambari", catalog = "")
@Entity
public class RoleSuccessCriteriaEntity {
  private Integer requestId;

  @Column(name = "request_id", insertable = false, updatable = false, nullable = false)
  @Id
  public Integer getRequestId() {
    return requestId;
  }

  public void setRequestId(Integer requestId) {
    this.requestId = requestId;
  }

  private Integer stageId;

  @Column(name = "stage_id", insertable = false, updatable = false, nullable = false)
  @Id
  public Integer getStageId() {
    return stageId;
  }

  public void setStageId(Integer stageId) {
    this.stageId = stageId;
  }

  private String role;

  @Column(name = "role")
  @Id
  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  private Double successFactor = 1d;

  @Column(name = "success_factor", nullable = false)
  @Basic
  public Double getSuccessFactor() {
    return successFactor;
  }

  public void setSuccessFactor(Double successFactor) {
    this.successFactor = successFactor;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RoleSuccessCriteriaEntity that = (RoleSuccessCriteriaEntity) o;

    if (requestId != null ? !requestId.equals(that.requestId) : that.requestId != null) return false;
    if (role != null ? !role.equals(that.role) : that.role != null) return false;
    if (stageId != null ? !stageId.equals(that.stageId) : that.stageId != null) return false;
    if (successFactor != null ? !successFactor.equals(that.successFactor) : that.successFactor != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = requestId != null ? requestId.hashCode() : 0;
    result = 31 * result + (stageId != null ? stageId.hashCode() : 0);
    result = 31 * result + (role != null ? role.hashCode() : 0);
    result = 31 * result + (successFactor != null ? successFactor.hashCode() : 0);
    return result;
  }

  private StageEntity stage;

  @ManyToOne
  @JoinColumns({@JoinColumn(name = "request_id", referencedColumnName = "request_id", nullable = false), @JoinColumn(name = "stage_id", referencedColumnName = "stage_id", nullable = false)})
  public StageEntity getStage() {
    return stage;
  }

  public void setStage(StageEntity stage) {
    this.stage = stage;
  }
}