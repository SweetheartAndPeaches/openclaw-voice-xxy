import React, { useState, useEffect, useCallback } from 'react';
import { useTaskStore } from '../store/taskStore';
import './TaskManager.css';

interface TaskManagerProps {
  userId: string;
  onTaskSelect?: (taskId: string) => void;
}

const TaskManager: React.FC<TaskManagerProps> = ({ userId, onTaskSelect }) => {
  const { 
    tasks, 
    loading, 
    error, 
    fetchTasks, 
    deleteTask, 
    deleteTasks,
    startPolling,
    stopPolling
  } = useTaskStore();
  
  const [selectedTasks, setSelectedTasks] = useState<string[]>([]);
  const [statusFilter, setStatusFilter] = useState<'all' | 'pending' | 'processing' | 'completed' | 'failed'>('all');
  const [isDeleting, setIsDeleting] = useState(false);
  
  // 初始化：获取任务并开始轮询
  useEffect(() => {
    if (userId) {
      fetchTasks(userId);
      startPolling(userId, 5000); // 每5秒轮询一次
    }
    
    return () => {
      stopPolling();
    };
  }, [userId, fetchTasks, startPolling, stopPolling]);
  
  // 过滤任务
  const filteredTasks = tasks.filter(task => {
    if (statusFilter === 'all') return true;
    return task.status === statusFilter;
  });
  
  // 处理任务选择
  const handleTaskSelect = (taskId: string) => {
    if (onTaskSelect) {
      onTaskSelect(taskId);
    }
  };
  
  // 处理复选框选择
  const handleCheckboxChange = (taskId: string, checked: boolean) => {
    if (checked) {
      setSelectedTasks(prev => [...prev, taskId]);
    } else {
      setSelectedTasks(prev => prev.filter(id => id !== taskId));
    }
  };
  
  // 全选/取消全选
  const handleSelectAll = (checked: boolean) => {
    if (checked) {
      setSelectedTasks(filteredTasks.map(task => task.taskId));
    } else {
      setSelectedTasks([]);
    }
  };
  
  // 删除单个任务
  const handleDeleteTask = async (taskId: string) => {
    if (window.confirm('确定要删除这个任务吗？')) {
      try {
        const response = await fetch(`/api/voice/tasks/${taskId}`, {
          method: 'DELETE'
        });
        
        if (response.ok) {
          deleteTask(taskId);
          setSelectedTasks(prev => prev.filter(id => id !== taskId));
        } else {
          throw new Error('删除失败');
        }
      } catch (error) {
        console.error('Error deleting task:', error);
        alert('删除任务失败，请稍后重试');
      }
    }
  };
  
  // 批量删除任务
  const handleBatchDelete = async () => {
    if (selectedTasks.length === 0) {
      alert('请先选择要删除的任务');
      return;
    }
    
    if (window.confirm(`确定要删除选中的 ${selectedTasks.length} 个任务吗？`)) {
      setIsDeleting(true);
      try {
        const deletePromises = selectedTasks.map(taskId => 
          fetch(`/api/voice/tasks/${taskId}`, {
            method: 'DELETE'
          })
        );
        
        const results = await Promise.all(deletePromises);
        const successfulDeletes = results.filter(result => result.ok).length;
        
        if (successfulDeletes > 0) {
          deleteTasks(selectedTasks);
          setSelectedTasks([]);
        }
        
        if (successfulDeletes < selectedTasks.length) {
          alert(`部分任务删除失败 (${successfulDeletes}/${selectedTasks.length})`);
        }
      } catch (error) {
        console.error('Error batch deleting tasks:', error);
        alert('批量删除任务失败，请稍后重试');
      } finally {
        setIsDeleting(false);
      }
    }
  };
  
  // 清空所有任务
  const handleClearAll = async () => {
    if (window.confirm('确定要清空所有任务吗？此操作不可恢复！')) {
      try {
        // 获取所有任务ID
        const allTaskIds = tasks.map(task => task.taskId);
        const deletePromises = allTaskIds.map(taskId => 
          fetch(`/api/voice/tasks/${taskId}`, {
            method: 'DELETE'
          })
        );
        
        await Promise.all(deletePromises);
        // 清空本地状态
        useTaskStore.getState().clearTasks();
        setSelectedTasks([]);
      } catch (error) {
        console.error('Error clearing all tasks:', error);
        alert('清空任务失败，请稍后重试');
      }
    }
  };
  
  // 获取状态显示文本
  const getStatusText = (status: string): string => {
    switch (status) {
      case 'pending': return '待处理';
      case 'processing': return '处理中';
      case 'completed': return '已完成';
      case 'failed': return '失败';
      default: return status;
    }
  };
  
  // 获取状态样式类
  const getStatusClass = (status: string): string => {
    switch (status) {
      case 'pending': return 'task-status-pending';
      case 'processing': return 'task-status-processing';
      case 'completed': return 'task-status-completed';
      case 'failed': return 'task-status-failed';
      default: return '';
    }
  };
  
  // 格式化时间
  const formatTime = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };
  
  return (
    <div className="task-manager">
      <h3 className="task-manager-title">任务管理</h3>
      
      {/* 状态筛选 */}
      <div className="task-manager-filters">
        <label htmlFor="status-filter" className="task-manager-filter-label">
          状态筛选:
        </label>
        <select
          id="status-filter"
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value as any)}
          className="task-manager-filter-select"
        >
          <option value="all">全部</option>
          <option value="pending">待处理</option>
          <option value="processing">处理中</option>
          <option value="completed">已完成</option>
          <option value="failed">失败</option>
        </select>
      </div>
      
      {/* 批量操作 */}
      {filteredTasks.length > 0 && (
        <div className="task-manager-batch-actions">
          <label className="task-manager-select-all">
            <input
              type="checkbox"
              checked={selectedTasks.length === filteredTasks.length && filteredTasks.length > 0}
              onChange={(e) => handleSelectAll(e.target.checked)}
            />
            全选 ({filteredTasks.length})
          </label>
          
          <div className="task-manager-batch-buttons">
            <button
              className="task-manager-batch-button task-manager-batch-delete"
              onClick={handleBatchDelete}
              disabled={selectedTasks.length === 0 || isDeleting}
            >
              {isDeleting ? '删除中...' : `删除选中 (${selectedTasks.length})`}
            </button>
            
            <button
              className="task-manager-batch-button task-manager-batch-clear"
              onClick={handleClearAll}
              disabled={isDeleting}
            >
              清空全部
            </button>
          </div>
        </div>
      )}
      
      {/* 加载状态 */}
      {loading && (
        <div className="task-manager-loading">
          <div className="task-manager-loading-spinner"></div>
          <span>加载任务中...</span>
        </div>
      )}
      
      {/* 错误状态 */}
      {error && (
        <div className="task-manager-error">
          <span>❌ {error}</span>
          <button 
            className="task-manager-retry-button"
            onClick={() => fetchTasks(userId)}
          >
            重试
          </button>
        </div>
      )}
      
      {/* 任务列表 */}
      {!loading && !error && (
        <div className="task-manager-list">
          {filteredTasks.length === 0 ? (
            <div className="task-manager-empty">
              {statusFilter === 'all' ? '暂无任务' : `暂无${getStatusText(statusFilter)}的任务`}
            </div>
          ) : (
            <>
              {filteredTasks.map((task) => (
                <div key={task.taskId} className="task-manager-item">
                  <div className="task-manager-item-header">
                    <div className="task-manager-item-checkbox">
                      <input
                        type="checkbox"
                        checked={selectedTasks.includes(task.taskId)}
                        onChange={(e) => handleCheckboxChange(task.taskId, e.target.checked)}
                      />
                    </div>
                    
                    <div className="task-manager-item-info">
                      <div className="task-manager-item-text">
                        {task.text.length > 50 ? task.text.substring(0, 50) + '...' : task.text}
                      </div>
                      <div className="task-manager-item-meta">
                        <span className={`task-manager-item-status ${getStatusClass(task.status)}`}>
                          {getStatusText(task.status)}
                        </span>
                        <span className="task-manager-item-time">
                          {formatTime(task.createdAt)}
                        </span>
                      </div>
                    </div>
                  </div>
                  
                  <div className="task-manager-item-actions">
                    <button
                      className="task-manager-item-action task-manager-item-play"
                      onClick={() => handleTaskSelect(task.taskId)}
                      disabled={task.status !== 'completed'}
                      title={task.status !== 'completed' ? '任务未完成' : '播放音频'}
                    >
                      ▶️
                    </button>
                    
                    <button
                      className="task-manager-item-action task-manager-item-delete"
                      onClick={() => handleDeleteTask(task.taskId)}
                      title="删除任务"
                    >
                      🗑️
                    </button>
                  </div>
                </div>
              ))}
            </>
          )}
        </div>
      )}
      
      {/* 自动刷新提示 */}
      <div className="task-manager-auto-refresh">
        🔁 自动刷新: 每 5 秒
      </div>
    </div>
  );
};

export default TaskManager;